/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  org.apache.commons.lang3.function.TriConsumer
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriConsumer;
import org.jspecify.annotations.Nullable;

public class TransportItemsBetweenContainers
extends Behavior<PathfinderMob> {
    public static final int TARGET_INTERACTION_TIME = 60;
    private static final int VISITED_POSITIONS_MEMORY_TIME = 6000;
    private static final int TRANSPORTED_ITEM_MAX_STACK_SIZE = 16;
    private static final int MAX_VISITED_POSITIONS = 10;
    private static final int MAX_UNREACHABLE_POSITIONS = 50;
    private static final int PASSENGER_MOB_TARGET_SEARCH_DISTANCE = 1;
    private static final int IDLE_COOLDOWN = 140;
    private static final double CLOSE_ENOUGH_TO_START_QUEUING_DISTANCE = 3.0;
    private static final double CLOSE_ENOUGH_TO_START_INTERACTING_WITH_TARGET_DISTANCE = 0.5;
    private static final double CLOSE_ENOUGH_TO_START_INTERACTING_WITH_TARGET_PATH_END_DISTANCE = 1.0;
    private static final double CLOSE_ENOUGH_TO_CONTINUE_INTERACTING_WITH_TARGET = 2.0;
    private final float speedModifier;
    private final int horizontalSearchDistance;
    private final int verticalSearchDistance;
    private final Predicate<BlockState> sourceBlockType;
    private final Predicate<BlockState> destinationBlockType;
    private final Predicate<TransportItemTarget> shouldQueueForTarget;
    private final Consumer<PathfinderMob> onStartTravelling;
    private final Map<ContainerInteractionState, OnTargetReachedInteraction> onTargetInteractionActions;
    private @Nullable TransportItemTarget target = null;
    private TransportItemState state;
    private @Nullable ContainerInteractionState interactionState;
    private int ticksSinceReachingTarget;

    public TransportItemsBetweenContainers(float $$0, Predicate<BlockState> $$1, Predicate<BlockState> $$2, int $$3, int $$4, Map<ContainerInteractionState, OnTargetReachedInteraction> $$5, Consumer<PathfinderMob> $$6, Predicate<TransportItemTarget> $$7) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.VISITED_BLOCK_POSITIONS, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.IS_PANICKING, (Object)((Object)MemoryStatus.VALUE_ABSENT)));
        this.speedModifier = $$0;
        this.sourceBlockType = $$1;
        this.destinationBlockType = $$2;
        this.horizontalSearchDistance = $$3;
        this.verticalSearchDistance = $$4;
        this.onStartTravelling = $$6;
        this.shouldQueueForTarget = $$7;
        this.onTargetInteractionActions = $$5;
        this.state = TransportItemState.TRAVELLING;
    }

    @Override
    protected void start(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        PathNavigation pathNavigation = $$1.getNavigation();
        if (pathNavigation instanceof GroundPathNavigation) {
            GroundPathNavigation $$3 = (GroundPathNavigation)pathNavigation;
            $$3.setCanPathToTargetsBelowSurface(true);
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, PathfinderMob $$1) {
        return !$$1.isLeashed();
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        return $$1.getBrain().getMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS).isEmpty() && !$$1.isPanicking() && !$$1.isLeashed();
    }

    @Override
    protected boolean timedOut(long $$0) {
        return false;
    }

    @Override
    protected void tick(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        boolean $$3 = this.updateInvalidTarget($$0, $$1);
        if (this.target == null) {
            this.stop($$0, $$1, $$2);
            return;
        }
        if ($$3) {
            return;
        }
        if (this.state.equals((Object)TransportItemState.QUEUING)) {
            this.onQueuingForTarget(this.target, $$0, $$1);
        }
        if (this.state.equals((Object)TransportItemState.TRAVELLING)) {
            this.onTravelToTarget(this.target, $$0, $$1);
        }
        if (this.state.equals((Object)TransportItemState.INTERACTING)) {
            this.onReachedTarget(this.target, $$0, $$1);
        }
    }

    private boolean updateInvalidTarget(ServerLevel $$0, PathfinderMob $$1) {
        if (!this.hasValidTarget($$0, $$1)) {
            this.stopTargetingCurrentTarget($$1);
            Optional<TransportItemTarget> $$2 = this.getTransportTarget($$0, $$1);
            if ($$2.isPresent()) {
                this.target = $$2.get();
                this.onStartTravelling($$1);
                this.setVisitedBlockPos($$1, $$0, this.target.pos);
                return true;
            }
            this.enterCooldownAfterNoMatchingTargetFound($$1);
            return true;
        }
        return false;
    }

    private void onQueuingForTarget(TransportItemTarget $$0, Level $$1, PathfinderMob $$2) {
        if (!this.isAnotherMobInteractingWithTarget($$0, $$1)) {
            this.resumeTravelling($$2);
        }
    }

    protected void onTravelToTarget(TransportItemTarget $$0, Level $$1, PathfinderMob $$2) {
        if (this.isWithinTargetDistance(3.0, $$0, $$1, $$2, this.getCenterPos($$2)) && this.isAnotherMobInteractingWithTarget($$0, $$1)) {
            this.startQueuing($$2);
        } else if (this.isWithinTargetDistance(TransportItemsBetweenContainers.getInteractionRange($$2), $$0, $$1, $$2, this.getCenterPos($$2))) {
            this.startOnReachedTargetInteraction($$0, $$2);
        } else {
            this.walkTowardsTarget($$2);
        }
    }

    private Vec3 getCenterPos(PathfinderMob $$0) {
        return this.setMiddleYPosition($$0, $$0.position());
    }

    protected void onReachedTarget(TransportItemTarget $$0, Level $$12, PathfinderMob $$22) {
        if (!this.isWithinTargetDistance(2.0, $$0, $$12, $$22, this.getCenterPos($$22))) {
            this.onStartTravelling($$22);
        } else {
            ++this.ticksSinceReachingTarget;
            this.onTargetInteraction($$0, $$22);
            if (this.ticksSinceReachingTarget >= 60) {
                this.doReachedTargetInteraction($$22, $$0.container, this::pickUpItems, ($$1, $$2) -> this.stopTargetingCurrentTarget($$22), this::putDownItem, ($$1, $$2) -> this.stopTargetingCurrentTarget($$22));
                this.onStartTravelling($$22);
            }
        }
    }

    private void startQueuing(PathfinderMob $$0) {
        this.stopInPlace($$0);
        this.setTransportingState(TransportItemState.QUEUING);
    }

    private void resumeTravelling(PathfinderMob $$0) {
        this.setTransportingState(TransportItemState.TRAVELLING);
        this.walkTowardsTarget($$0);
    }

    private void walkTowardsTarget(PathfinderMob $$0) {
        if (this.target != null) {
            BehaviorUtils.setWalkAndLookTargetMemories((LivingEntity)$$0, this.target.pos, this.speedModifier, 0);
        }
    }

    private void startOnReachedTargetInteraction(TransportItemTarget $$0, PathfinderMob $$1) {
        this.doReachedTargetInteraction($$1, $$0.container, this.onReachedInteraction(ContainerInteractionState.PICKUP_ITEM), this.onReachedInteraction(ContainerInteractionState.PICKUP_NO_ITEM), this.onReachedInteraction(ContainerInteractionState.PLACE_ITEM), this.onReachedInteraction(ContainerInteractionState.PLACE_NO_ITEM));
        this.setTransportingState(TransportItemState.INTERACTING);
    }

    private void onStartTravelling(PathfinderMob $$0) {
        this.onStartTravelling.accept($$0);
        this.setTransportingState(TransportItemState.TRAVELLING);
        this.interactionState = null;
        this.ticksSinceReachingTarget = 0;
    }

    private BiConsumer<PathfinderMob, Container> onReachedInteraction(ContainerInteractionState $$0) {
        return ($$1, $$2) -> this.setInteractionState($$0);
    }

    private void setTransportingState(TransportItemState $$0) {
        this.state = $$0;
    }

    private void setInteractionState(ContainerInteractionState $$0) {
        this.interactionState = $$0;
    }

    private void onTargetInteraction(TransportItemTarget $$0, PathfinderMob $$1) {
        $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker($$0.pos));
        this.stopInPlace($$1);
        if (this.interactionState != null) {
            Optional.ofNullable(this.onTargetInteractionActions.get((Object)this.interactionState)).ifPresent($$2 -> $$2.accept($$1, $$0, this.ticksSinceReachingTarget));
        }
    }

    private void doReachedTargetInteraction(PathfinderMob $$0, Container $$1, BiConsumer<PathfinderMob, Container> $$2, BiConsumer<PathfinderMob, Container> $$3, BiConsumer<PathfinderMob, Container> $$4, BiConsumer<PathfinderMob, Container> $$5) {
        if (TransportItemsBetweenContainers.isPickingUpItems($$0)) {
            if (TransportItemsBetweenContainers.matchesGettingItemsRequirement($$1)) {
                $$2.accept($$0, $$1);
            } else {
                $$3.accept($$0, $$1);
            }
        } else if (TransportItemsBetweenContainers.matchesLeavingItemsRequirement($$0, $$1)) {
            $$4.accept($$0, $$1);
        } else {
            $$5.accept($$0, $$1);
        }
    }

    private Optional<TransportItemTarget> getTransportTarget(ServerLevel $$0, PathfinderMob $$1) {
        AABB $$2 = this.getTargetSearchArea($$1);
        Set<GlobalPos> $$3 = TransportItemsBetweenContainers.getVisitedPositions($$1);
        Set<GlobalPos> $$4 = TransportItemsBetweenContainers.getUnreachablePositions($$1);
        List<ChunkPos> $$5 = ChunkPos.rangeClosed(new ChunkPos($$1.blockPosition()), Math.floorDiv(this.getHorizontalSearchDistance($$1), 16) + 1).toList();
        TransportItemTarget $$6 = null;
        double $$7 = 3.4028234663852886E38;
        for (ChunkPos $$8 : $$5) {
            LevelChunk $$9 = $$0.getChunkSource().getChunkNow($$8.x, $$8.z);
            if ($$9 == null) continue;
            for (BlockEntity $$10 : $$9.getBlockEntities().values()) {
                TransportItemTarget $$13;
                ChestBlockEntity $$11;
                double $$12;
                if (!($$10 instanceof ChestBlockEntity) || !(($$12 = ($$11 = (ChestBlockEntity)$$10).getBlockPos().distToCenterSqr($$1.position())) < $$7) || ($$13 = this.isTargetValidToPick($$1, $$0, $$11, $$3, $$4, $$2)) == null) continue;
                $$6 = $$13;
                $$7 = $$12;
            }
        }
        return $$6 == null ? Optional.empty() : Optional.of($$6);
    }

    private @Nullable TransportItemTarget isTargetValidToPick(PathfinderMob $$0, Level $$1, BlockEntity $$2, Set<GlobalPos> $$3, Set<GlobalPos> $$4, AABB $$5) {
        BlockPos $$6 = $$2.getBlockPos();
        boolean $$7 = $$5.contains($$6.getX(), $$6.getY(), $$6.getZ());
        if (!$$7) {
            return null;
        }
        TransportItemTarget $$8 = TransportItemTarget.tryCreatePossibleTarget($$2, $$1);
        if ($$8 == null) {
            return null;
        }
        boolean $$9 = this.isWantedBlock($$0, $$8.state) && !this.isPositionAlreadyVisited($$3, $$4, $$8, $$1) && !this.isContainerLocked($$8);
        return $$9 ? $$8 : null;
    }

    private boolean isContainerLocked(TransportItemTarget $$0) {
        BaseContainerBlockEntity $$1;
        BlockEntity blockEntity = $$0.blockEntity;
        return blockEntity instanceof BaseContainerBlockEntity && ($$1 = (BaseContainerBlockEntity)blockEntity).isLocked();
    }

    private boolean hasValidTarget(Level $$0, PathfinderMob $$1) {
        boolean $$2;
        boolean bl = $$2 = this.target != null && this.isWantedBlock($$1, this.target.state) && this.targetHasNotChanged($$0, this.target);
        if ($$2 && !this.isTargetBlocked($$0, this.target)) {
            if (!this.state.equals((Object)TransportItemState.TRAVELLING)) {
                return true;
            }
            if (this.hasValidTravellingPath($$0, this.target, $$1)) {
                return true;
            }
            this.markVisitedBlockPosAsUnreachable($$1, $$0, this.target.pos);
        }
        return false;
    }

    private boolean hasValidTravellingPath(Level $$0, TransportItemTarget $$1, PathfinderMob $$2) {
        Path $$3 = $$2.getNavigation().getPath() == null ? $$2.getNavigation().createPath($$1.pos, 0) : $$2.getNavigation().getPath();
        Vec3 $$4 = this.getPositionToReachTargetFrom($$3, $$2);
        boolean $$5 = this.isWithinTargetDistance(TransportItemsBetweenContainers.getInteractionRange($$2), $$1, $$0, $$2, $$4);
        boolean $$6 = $$3 == null && !$$5;
        return $$6 || this.targetIsReachableFromPosition($$0, $$5, $$4, $$1, $$2);
    }

    private Vec3 getPositionToReachTargetFrom(@Nullable Path $$0, PathfinderMob $$1) {
        boolean $$2 = $$0 == null || $$0.getEndNode() == null;
        Vec3 $$3 = $$2 ? $$1.position() : $$0.getEndNode().asBlockPos().getBottomCenter();
        return this.setMiddleYPosition($$1, $$3);
    }

    private Vec3 setMiddleYPosition(PathfinderMob $$0, Vec3 $$1) {
        return $$1.add(0.0, $$0.getBoundingBox().getYsize() / 2.0, 0.0);
    }

    private boolean isTargetBlocked(Level $$0, TransportItemTarget $$1) {
        return ChestBlock.isChestBlockedAt($$0, $$1.pos);
    }

    private boolean targetHasNotChanged(Level $$0, TransportItemTarget $$1) {
        return $$1.blockEntity.equals($$0.getBlockEntity($$1.pos));
    }

    private Stream<TransportItemTarget> getConnectedTargets(TransportItemTarget $$0, Level $$1) {
        if ($$0.state.getValueOrElse(ChestBlock.TYPE, ChestType.SINGLE) != ChestType.SINGLE) {
            TransportItemTarget $$2 = TransportItemTarget.tryCreatePossibleTarget(ChestBlock.getConnectedBlockPos($$0.pos, $$0.state), $$1);
            return $$2 != null ? Stream.of($$0, $$2) : Stream.of($$0);
        }
        return Stream.of($$0);
    }

    private AABB getTargetSearchArea(PathfinderMob $$0) {
        int $$1 = this.getHorizontalSearchDistance($$0);
        return new AABB($$0.blockPosition()).inflate($$1, this.getVerticalSearchDistance($$0), $$1);
    }

    private int getHorizontalSearchDistance(PathfinderMob $$0) {
        return $$0.isPassenger() ? 1 : this.horizontalSearchDistance;
    }

    private int getVerticalSearchDistance(PathfinderMob $$0) {
        return $$0.isPassenger() ? 1 : this.verticalSearchDistance;
    }

    private static Set<GlobalPos> getVisitedPositions(PathfinderMob $$0) {
        return $$0.getBrain().getMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).orElse(Set.of());
    }

    private static Set<GlobalPos> getUnreachablePositions(PathfinderMob $$0) {
        return $$0.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS).orElse(Set.of());
    }

    private boolean isPositionAlreadyVisited(Set<GlobalPos> $$0, Set<GlobalPos> $$12, TransportItemTarget $$22, Level $$3) {
        return this.getConnectedTargets($$22, $$3).map($$1 -> new GlobalPos($$3.dimension(), $$1.pos)).anyMatch($$2 -> $$0.contains($$2) || $$12.contains($$2));
    }

    private static boolean hasFinishedPath(PathfinderMob $$0) {
        return $$0.getNavigation().getPath() != null && $$0.getNavigation().getPath().isDone();
    }

    protected void setVisitedBlockPos(PathfinderMob $$0, Level $$1, BlockPos $$2) {
        HashSet<GlobalPos> $$3 = new HashSet<GlobalPos>(TransportItemsBetweenContainers.getVisitedPositions($$0));
        $$3.add(new GlobalPos($$1.dimension(), $$2));
        if ($$3.size() > 10) {
            this.enterCooldownAfterNoMatchingTargetFound($$0);
        } else {
            $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.VISITED_BLOCK_POSITIONS, $$3, 6000L);
        }
    }

    protected void markVisitedBlockPosAsUnreachable(PathfinderMob $$0, Level $$1, BlockPos $$2) {
        HashSet<GlobalPos> $$3 = new HashSet<GlobalPos>(TransportItemsBetweenContainers.getVisitedPositions($$0));
        $$3.remove(new GlobalPos($$1.dimension(), $$2));
        HashSet<GlobalPos> $$4 = new HashSet<GlobalPos>(TransportItemsBetweenContainers.getUnreachablePositions($$0));
        $$4.add(new GlobalPos($$1.dimension(), $$2));
        if ($$4.size() > 50) {
            this.enterCooldownAfterNoMatchingTargetFound($$0);
        } else {
            $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.VISITED_BLOCK_POSITIONS, $$3, 6000L);
            $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, $$4, 6000L);
        }
    }

    private boolean isWantedBlock(PathfinderMob $$0, BlockState $$1) {
        return TransportItemsBetweenContainers.isPickingUpItems($$0) ? this.sourceBlockType.test($$1) : this.destinationBlockType.test($$1);
    }

    private static double getInteractionRange(PathfinderMob $$0) {
        return TransportItemsBetweenContainers.hasFinishedPath($$0) ? 1.0 : 0.5;
    }

    private boolean isWithinTargetDistance(double $$0, TransportItemTarget $$1, Level $$2, PathfinderMob $$3, Vec3 $$4) {
        AABB $$5 = $$3.getBoundingBox();
        AABB $$6 = AABB.ofSize($$4, $$5.getXsize(), $$5.getYsize(), $$5.getZsize());
        return $$1.state.getCollisionShape($$2, $$1.pos).bounds().inflate($$0, 0.5, $$0).move($$1.pos).intersects($$6);
    }

    private boolean targetIsReachableFromPosition(Level $$0, boolean $$1, Vec3 $$2, TransportItemTarget $$3, PathfinderMob $$4) {
        return $$1 && this.canSeeAnyTargetSide($$3, $$0, $$4, $$2);
    }

    private boolean canSeeAnyTargetSide(TransportItemTarget $$0, Level $$12, PathfinderMob $$2, Vec3 $$32) {
        Vec3 $$4 = $$0.pos.getCenter();
        return Direction.stream().map($$1 -> $$4.add(0.5 * (double)$$1.getStepX(), 0.5 * (double)$$1.getStepY(), 0.5 * (double)$$1.getStepZ())).map($$3 -> $$12.clip(new ClipContext($$32, (Vec3)$$3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, $$2))).anyMatch($$1 -> $$1.getType() == HitResult.Type.BLOCK && $$1.getBlockPos().equals($$0.pos));
    }

    private boolean isAnotherMobInteractingWithTarget(TransportItemTarget $$0, Level $$1) {
        return this.getConnectedTargets($$0, $$1).anyMatch(this.shouldQueueForTarget);
    }

    private static boolean isPickingUpItems(PathfinderMob $$0) {
        return $$0.getMainHandItem().isEmpty();
    }

    private static boolean matchesGettingItemsRequirement(Container $$0) {
        return !$$0.isEmpty();
    }

    private static boolean matchesLeavingItemsRequirement(PathfinderMob $$0, Container $$1) {
        return $$1.isEmpty() || TransportItemsBetweenContainers.hasItemMatchingHandItem($$0, $$1);
    }

    private static boolean hasItemMatchingHandItem(PathfinderMob $$0, Container $$1) {
        ItemStack $$2 = $$0.getMainHandItem();
        for (ItemStack $$3 : $$1) {
            if (!ItemStack.isSameItem($$3, $$2)) continue;
            return true;
        }
        return false;
    }

    private void pickUpItems(PathfinderMob $$0, Container $$1) {
        $$0.setItemSlot(EquipmentSlot.MAINHAND, TransportItemsBetweenContainers.pickupItemFromContainer($$1));
        $$0.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        $$1.setChanged();
        this.clearMemoriesAfterMatchingTargetFound($$0);
    }

    private void putDownItem(PathfinderMob $$0, Container $$1) {
        ItemStack $$2 = TransportItemsBetweenContainers.addItemsToContainer($$0, $$1);
        $$1.setChanged();
        $$0.setItemSlot(EquipmentSlot.MAINHAND, $$2);
        if ($$2.isEmpty()) {
            this.clearMemoriesAfterMatchingTargetFound($$0);
        } else {
            this.stopTargetingCurrentTarget($$0);
        }
    }

    private static ItemStack pickupItemFromContainer(Container $$0) {
        int $$1 = 0;
        for (ItemStack $$2 : $$0) {
            if (!$$2.isEmpty()) {
                int $$3 = Math.min($$2.getCount(), 16);
                return $$0.removeItem($$1, $$3);
            }
            ++$$1;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack addItemsToContainer(PathfinderMob $$0, Container $$1) {
        int $$2 = 0;
        ItemStack $$3 = $$0.getMainHandItem();
        for (ItemStack $$4 : $$1) {
            if ($$4.isEmpty()) {
                $$1.setItem($$2, $$3);
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameComponents($$4, $$3) && $$4.getCount() < $$4.getMaxStackSize()) {
                int $$5 = $$4.getMaxStackSize() - $$4.getCount();
                int $$6 = Math.min($$5, $$3.getCount());
                $$4.setCount($$4.getCount() + $$6);
                $$3.setCount($$3.getCount() - $$5);
                $$1.setItem($$2, $$4);
                if ($$3.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
            ++$$2;
        }
        return $$3;
    }

    protected void stopTargetingCurrentTarget(PathfinderMob $$0) {
        this.ticksSinceReachingTarget = 0;
        this.target = null;
        $$0.getNavigation().stop();
        $$0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    protected void clearMemoriesAfterMatchingTargetFound(PathfinderMob $$0) {
        this.stopTargetingCurrentTarget($$0);
        $$0.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        $$0.getBrain().eraseMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    private void enterCooldownAfterNoMatchingTargetFound(PathfinderMob $$0) {
        this.stopTargetingCurrentTarget($$0);
        $$0.getBrain().setMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, 140);
        $$0.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        $$0.getBrain().eraseMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    @Override
    protected void stop(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        this.onStartTravelling($$1);
        PathNavigation pathNavigation = $$1.getNavigation();
        if (pathNavigation instanceof GroundPathNavigation) {
            GroundPathNavigation $$3 = (GroundPathNavigation)pathNavigation;
            $$3.setCanPathToTargetsBelowSurface(false);
        }
    }

    private void stopInPlace(PathfinderMob $$0) {
        $$0.getNavigation().stop();
        $$0.setXxa(0.0f);
        $$0.setYya(0.0f);
        $$0.setSpeed(0.0f);
        $$0.setDeltaMovement(0.0, $$0.getDeltaMovement().y, 0.0);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }

    public static final class TransportItemTarget
    extends Record {
        final BlockPos pos;
        final Container container;
        final BlockEntity blockEntity;
        final BlockState state;

        public TransportItemTarget(BlockPos $$0, Container $$1, BlockEntity $$2, BlockState $$3) {
            this.pos = $$0;
            this.container = $$1;
            this.blockEntity = $$2;
            this.state = $$3;
        }

        public static @Nullable TransportItemTarget tryCreatePossibleTarget(BlockEntity $$0, Level $$1) {
            BlockPos $$2 = $$0.getBlockPos();
            BlockState $$3 = $$0.getBlockState();
            Container $$4 = TransportItemTarget.getBlockEntityContainer($$0, $$3, $$1, $$2);
            if ($$4 != null) {
                return new TransportItemTarget($$2, $$4, $$0, $$3);
            }
            return null;
        }

        public static @Nullable TransportItemTarget tryCreatePossibleTarget(BlockPos $$0, Level $$1) {
            BlockEntity $$2 = $$1.getBlockEntity($$0);
            return $$2 == null ? null : TransportItemTarget.tryCreatePossibleTarget($$2, $$1);
        }

        private static @Nullable Container getBlockEntityContainer(BlockEntity $$0, BlockState $$1, Level $$2, BlockPos $$3) {
            Block block = $$1.getBlock();
            if (block instanceof ChestBlock) {
                ChestBlock $$4 = (ChestBlock)block;
                return ChestBlock.getContainer($$4, $$1, $$2, $$3, false);
            }
            if ($$0 instanceof Container) {
                Container $$5 = (Container)((Object)$$0);
                return $$5;
            }
            return null;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TransportItemTarget.class, "pos;container;blockEntity;state", "pos", "container", "blockEntity", "state"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TransportItemTarget.class, "pos;container;blockEntity;state", "pos", "container", "blockEntity", "state"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TransportItemTarget.class, "pos;container;blockEntity;state", "pos", "container", "blockEntity", "state"}, this, $$0);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public Container container() {
            return this.container;
        }

        public BlockEntity blockEntity() {
            return this.blockEntity;
        }

        public BlockState state() {
            return this.state;
        }
    }

    public static enum TransportItemState {
        TRAVELLING,
        QUEUING,
        INTERACTING;

    }

    public static enum ContainerInteractionState {
        PICKUP_ITEM,
        PICKUP_NO_ITEM,
        PLACE_ITEM,
        PLACE_NO_ITEM;

    }

    @FunctionalInterface
    public static interface OnTargetReachedInteraction
    extends TriConsumer<PathfinderMob, TransportItemTarget, Integer> {
    }
}

