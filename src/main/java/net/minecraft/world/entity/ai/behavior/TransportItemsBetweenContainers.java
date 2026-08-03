package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
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
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.apache.commons.lang3.function.TriConsumer;
import org.jspecify.annotations.Nullable;

public class TransportItemsBetweenContainers extends Behavior<net.minecraft.world.entity.PathfinderMob> {
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
   private final Predicate<TransportItemsBetweenContainers.TransportItemTarget> shouldQueueForTarget;
   private final Consumer<net.minecraft.world.entity.PathfinderMob> onStartTravelling;
   private final Map<TransportItemsBetweenContainers.ContainerInteractionState, TransportItemsBetweenContainers.OnTargetReachedInteraction> onTargetInteractionActions;
   @Nullable
   private TransportItemsBetweenContainers.TransportItemTarget target = null;
   private TransportItemsBetweenContainers.TransportItemState state;
   @Nullable
   private TransportItemsBetweenContainers.ContainerInteractionState interactionState;
   private int ticksSinceReachingTarget;

   public TransportItemsBetweenContainers(
      float $$0,
      Predicate<BlockState> $$1,
      Predicate<BlockState> $$2,
      int $$3,
      int $$4,
      Map<TransportItemsBetweenContainers.ContainerInteractionState, TransportItemsBetweenContainers.OnTargetReachedInteraction> $$5,
      Consumer<net.minecraft.world.entity.PathfinderMob> $$6,
      Predicate<TransportItemsBetweenContainers.TransportItemTarget> $$7
   ) {
      super(
         ImmutableMap.of(
            MemoryModuleType.VISITED_BLOCK_POSITIONS,
            MemoryStatus.REGISTERED,
            MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS,
            MemoryStatus.REGISTERED,
            MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.IS_PANICKING,
            MemoryStatus.VALUE_ABSENT
         )
      );
      this.speedModifier = $$0;
      this.sourceBlockType = $$1;
      this.destinationBlockType = $$2;
      this.horizontalSearchDistance = $$3;
      this.verticalSearchDistance = $$4;
      this.onStartTravelling = $$6;
      this.shouldQueueForTarget = $$7;
      this.onTargetInteractionActions = $$5;
      this.state = TransportItemsBetweenContainers.TransportItemState.TRAVELLING;
   }

   protected void start(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      if ($$1.getNavigation() instanceof GroundPathNavigation $$3) {
         $$3.setCanPathToTargetsBelowSurface(true);
      }
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      return !$$1.isLeashed();
   }

   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      return $$1.getBrain().getMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS).isEmpty() && !$$1.isPanicking() && !$$1.isLeashed();
   }

   @Override
   protected boolean timedOut(long $$0) {
      return false;
   }

   protected void tick(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      boolean $$3 = this.updateInvalidTarget($$0, $$1);
      if (this.target == null) {
         this.stop($$0, $$1, $$2);
      } else if (!$$3) {
         if (this.state.equals(TransportItemsBetweenContainers.TransportItemState.QUEUING)) {
            this.onQueuingForTarget(this.target, $$0, $$1);
         }

         if (this.state.equals(TransportItemsBetweenContainers.TransportItemState.TRAVELLING)) {
            this.onTravelToTarget(this.target, $$0, $$1);
         }

         if (this.state.equals(TransportItemsBetweenContainers.TransportItemState.INTERACTING)) {
            this.onReachedTarget(this.target, $$0, $$1);
         }
      }
   }

   private boolean updateInvalidTarget(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      if (!this.hasValidTarget($$0, $$1)) {
         this.stopTargetingCurrentTarget($$1);
         Optional<TransportItemsBetweenContainers.TransportItemTarget> $$2 = this.getTransportTarget($$0, $$1);
         if ($$2.isPresent()) {
            this.target = $$2.get();
            this.onStartTravelling($$1);
            this.setVisitedBlockPos($$1, $$0, this.target.pos);
            return true;
         } else {
            this.enterCooldownAfterNoMatchingTargetFound($$1);
            return true;
         }
      } else {
         return false;
      }
   }

   private void onQueuingForTarget(TransportItemsBetweenContainers.TransportItemTarget $$0, Level $$1, net.minecraft.world.entity.PathfinderMob $$2) {
      if (!this.isAnotherMobInteractingWithTarget($$0, $$1)) {
         this.resumeTravelling($$2);
      }
   }

   protected void onTravelToTarget(TransportItemsBetweenContainers.TransportItemTarget $$0, Level $$1, net.minecraft.world.entity.PathfinderMob $$2) {
      if (this.isWithinTargetDistance(3.0, $$0, $$1, $$2, this.getCenterPos($$2)) && this.isAnotherMobInteractingWithTarget($$0, $$1)) {
         this.startQueuing($$2);
      } else if (this.isWithinTargetDistance(getInteractionRange($$2), $$0, $$1, $$2, this.getCenterPos($$2))) {
         this.startOnReachedTargetInteraction($$0, $$2);
      } else {
         this.walkTowardsTarget($$2);
      }
   }

   private Vec3 getCenterPos(net.minecraft.world.entity.PathfinderMob $$0) {
      return this.setMiddleYPosition($$0, $$0.position());
   }

   protected void onReachedTarget(TransportItemsBetweenContainers.TransportItemTarget $$0, Level $$1, net.minecraft.world.entity.PathfinderMob $$2) {
      if (!this.isWithinTargetDistance(2.0, $$0, $$1, $$2, this.getCenterPos($$2))) {
         this.onStartTravelling($$2);
      } else {
         this.ticksSinceReachingTarget++;
         this.onTargetInteraction($$0, $$2);
         if (this.ticksSinceReachingTarget >= 60) {
            this.doReachedTargetInteraction(
               $$2,
               $$0.container,
               this::pickUpItems,
               ($$1x, $$2x) -> this.stopTargetingCurrentTarget($$2),
               this::putDownItem,
               ($$1x, $$2x) -> this.stopTargetingCurrentTarget($$2)
            );
            this.onStartTravelling($$2);
         }
      }
   }

   private void startQueuing(net.minecraft.world.entity.PathfinderMob $$0) {
      this.stopInPlace($$0);
      this.setTransportingState(TransportItemsBetweenContainers.TransportItemState.QUEUING);
   }

   private void resumeTravelling(net.minecraft.world.entity.PathfinderMob $$0) {
      this.setTransportingState(TransportItemsBetweenContainers.TransportItemState.TRAVELLING);
      this.walkTowardsTarget($$0);
   }

   private void walkTowardsTarget(net.minecraft.world.entity.PathfinderMob $$0) {
      if (this.target != null) {
         BehaviorUtils.setWalkAndLookTargetMemories($$0, this.target.pos, this.speedModifier, 0);
      }
   }

   private void startOnReachedTargetInteraction(TransportItemsBetweenContainers.TransportItemTarget $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      this.doReachedTargetInteraction(
         $$1,
         $$0.container,
         this.onReachedInteraction(TransportItemsBetweenContainers.ContainerInteractionState.PICKUP_ITEM),
         this.onReachedInteraction(TransportItemsBetweenContainers.ContainerInteractionState.PICKUP_NO_ITEM),
         this.onReachedInteraction(TransportItemsBetweenContainers.ContainerInteractionState.PLACE_ITEM),
         this.onReachedInteraction(TransportItemsBetweenContainers.ContainerInteractionState.PLACE_NO_ITEM)
      );
      this.setTransportingState(TransportItemsBetweenContainers.TransportItemState.INTERACTING);
   }

   private void onStartTravelling(net.minecraft.world.entity.PathfinderMob $$0) {
      this.onStartTravelling.accept($$0);
      this.setTransportingState(TransportItemsBetweenContainers.TransportItemState.TRAVELLING);
      this.interactionState = null;
      this.ticksSinceReachingTarget = 0;
   }

   private BiConsumer<net.minecraft.world.entity.PathfinderMob, Container> onReachedInteraction(TransportItemsBetweenContainers.ContainerInteractionState $$0) {
      return ($$1, $$2) -> this.setInteractionState($$0);
   }

   private void setTransportingState(TransportItemsBetweenContainers.TransportItemState $$0) {
      this.state = $$0;
   }

   private void setInteractionState(TransportItemsBetweenContainers.ContainerInteractionState $$0) {
      this.interactionState = $$0;
   }

   private void onTargetInteraction(TransportItemsBetweenContainers.TransportItemTarget $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker($$0.pos));
      this.stopInPlace($$1);
      if (this.interactionState != null) {
         Optional.ofNullable(this.onTargetInteractionActions.get(this.interactionState)).ifPresent($$2 -> $$2.accept($$1, $$0, this.ticksSinceReachingTarget));
      }
   }

   private void doReachedTargetInteraction(
      net.minecraft.world.entity.PathfinderMob $$0,
      Container $$1,
      BiConsumer<net.minecraft.world.entity.PathfinderMob, Container> $$2,
      BiConsumer<net.minecraft.world.entity.PathfinderMob, Container> $$3,
      BiConsumer<net.minecraft.world.entity.PathfinderMob, Container> $$4,
      BiConsumer<net.minecraft.world.entity.PathfinderMob, Container> $$5
   ) {
      if (isPickingUpItems($$0)) {
         if (matchesGettingItemsRequirement($$1)) {
            $$2.accept($$0, $$1);
         } else {
            $$3.accept($$0, $$1);
         }
      } else if (matchesLeavingItemsRequirement($$0, $$1)) {
         $$4.accept($$0, $$1);
      } else {
         $$5.accept($$0, $$1);
      }
   }

   private Optional<TransportItemsBetweenContainers.TransportItemTarget> getTransportTarget(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      AABB $$2 = this.getTargetSearchArea($$1);
      Set<GlobalPos> $$3 = getVisitedPositions($$1);
      Set<GlobalPos> $$4 = getUnreachablePositions($$1);
      List<ChunkPos> $$5 = ChunkPos.rangeClosed(new ChunkPos($$1.blockPosition()), Math.floorDiv(this.getHorizontalSearchDistance($$1), 16) + 1).toList();
      TransportItemsBetweenContainers.TransportItemTarget $$6 = null;
      double $$7 = Float.MAX_VALUE;

      for (ChunkPos $$8 : $$5) {
         LevelChunk $$9 = $$0.getChunkSource().getChunkNow($$8.x, $$8.z);
         if ($$9 != null) {
            for (BlockEntity $$10 : $$9.getBlockEntities().values()) {
               if ($$10 instanceof ChestBlockEntity $$11) {
                  double $$12 = $$11.getBlockPos().distToCenterSqr($$1.position());
                  if ($$12 < $$7) {
                     TransportItemsBetweenContainers.TransportItemTarget $$13 = this.isTargetValidToPick($$1, $$0, $$11, $$3, $$4, $$2);
                     if ($$13 != null) {
                        $$6 = $$13;
                        $$7 = $$12;
                     }
                  }
               }
            }
         }
      }

      return $$6 == null ? Optional.empty() : Optional.of($$6);
   }

   @Nullable
   private TransportItemsBetweenContainers.TransportItemTarget isTargetValidToPick(
      net.minecraft.world.entity.PathfinderMob $$0, Level $$1, BlockEntity $$2, Set<GlobalPos> $$3, Set<GlobalPos> $$4, AABB $$5
   ) {
      BlockPos $$6 = $$2.getBlockPos();
      boolean $$7 = $$5.contains($$6.getX(), $$6.getY(), $$6.getZ());
      if (!$$7) {
         return null;
      } else {
         TransportItemsBetweenContainers.TransportItemTarget $$8 = TransportItemsBetweenContainers.TransportItemTarget.tryCreatePossibleTarget($$2, $$1);
         if ($$8 == null) {
            return null;
         } else {
            boolean $$9 = this.isWantedBlock($$0, $$8.state) && !this.isPositionAlreadyVisited($$3, $$4, $$8, $$1) && !this.isContainerLocked($$8);
            return $$9 ? $$8 : null;
         }
      }
   }

   private boolean isContainerLocked(TransportItemsBetweenContainers.TransportItemTarget $$0) {
      return $$0.blockEntity instanceof BaseContainerBlockEntity $$1 && $$1.isLocked();
   }

   private boolean hasValidTarget(Level $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      boolean $$2 = this.target != null && this.isWantedBlock($$1, this.target.state) && this.targetHasNotChanged($$0, this.target);
      if ($$2 && !this.isTargetBlocked($$0, this.target)) {
         if (!this.state.equals(TransportItemsBetweenContainers.TransportItemState.TRAVELLING)) {
            return true;
         }

         if (this.hasValidTravellingPath($$0, this.target, $$1)) {
            return true;
         }

         this.markVisitedBlockPosAsUnreachable($$1, $$0, this.target.pos);
      }

      return false;
   }

   private boolean hasValidTravellingPath(Level $$0, TransportItemsBetweenContainers.TransportItemTarget $$1, net.minecraft.world.entity.PathfinderMob $$2) {
      Path $$3 = $$2.getNavigation().getPath() == null ? $$2.getNavigation().createPath($$1.pos, 0) : $$2.getNavigation().getPath();
      Vec3 $$4 = this.getPositionToReachTargetFrom($$3, $$2);
      boolean $$5 = this.isWithinTargetDistance(getInteractionRange($$2), $$1, $$0, $$2, $$4);
      boolean $$6 = $$3 == null && !$$5;
      return $$6 || this.targetIsReachableFromPosition($$0, $$5, $$4, $$1, $$2);
   }

   private Vec3 getPositionToReachTargetFrom(@Nullable Path $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      boolean $$2 = $$0 == null || $$0.getEndNode() == null;
      Vec3 $$3 = $$2 ? $$1.position() : $$0.getEndNode().asBlockPos().getBottomCenter();
      return this.setMiddleYPosition($$1, $$3);
   }

   private Vec3 setMiddleYPosition(net.minecraft.world.entity.PathfinderMob $$0, Vec3 $$1) {
      return $$1.add(0.0, $$0.getBoundingBox().getYsize() / 2.0, 0.0);
   }

   private boolean isTargetBlocked(Level $$0, TransportItemsBetweenContainers.TransportItemTarget $$1) {
      return ChestBlock.isChestBlockedAt($$0, $$1.pos);
   }

   private boolean targetHasNotChanged(Level $$0, TransportItemsBetweenContainers.TransportItemTarget $$1) {
      return $$1.blockEntity.equals($$0.getBlockEntity($$1.pos));
   }

   private Stream<TransportItemsBetweenContainers.TransportItemTarget> getConnectedTargets(TransportItemsBetweenContainers.TransportItemTarget $$0, Level $$1) {
      if ($$0.state.getValueOrElse(ChestBlock.TYPE, ChestType.SINGLE) != ChestType.SINGLE) {
         TransportItemsBetweenContainers.TransportItemTarget $$2 = TransportItemsBetweenContainers.TransportItemTarget.tryCreatePossibleTarget(
            ChestBlock.getConnectedBlockPos($$0.pos, $$0.state), $$1
         );
         return $$2 != null ? Stream.of($$0, $$2) : Stream.of($$0);
      } else {
         return Stream.of($$0);
      }
   }

   private AABB getTargetSearchArea(net.minecraft.world.entity.PathfinderMob $$0) {
      int $$1 = this.getHorizontalSearchDistance($$0);
      return new AABB($$0.blockPosition()).inflate($$1, this.getVerticalSearchDistance($$0), $$1);
   }

   private int getHorizontalSearchDistance(net.minecraft.world.entity.PathfinderMob $$0) {
      return $$0.isPassenger() ? 1 : this.horizontalSearchDistance;
   }

   private int getVerticalSearchDistance(net.minecraft.world.entity.PathfinderMob $$0) {
      return $$0.isPassenger() ? 1 : this.verticalSearchDistance;
   }

   private static Set<GlobalPos> getVisitedPositions(net.minecraft.world.entity.PathfinderMob $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).orElse(Set.of());
   }

   private static Set<GlobalPos> getUnreachablePositions(net.minecraft.world.entity.PathfinderMob $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS).orElse(Set.of());
   }

   private boolean isPositionAlreadyVisited(Set<GlobalPos> $$0, Set<GlobalPos> $$1, TransportItemsBetweenContainers.TransportItemTarget $$2, Level $$3) {
      return this.getConnectedTargets($$2, $$3)
         .map($$1x -> new GlobalPos($$3.dimension(), $$1x.pos))
         .anyMatch($$2x -> $$0.contains($$2x) || $$1.contains($$2x));
   }

   private static boolean hasFinishedPath(net.minecraft.world.entity.PathfinderMob $$0) {
      return $$0.getNavigation().getPath() != null && $$0.getNavigation().getPath().isDone();
   }

   protected void setVisitedBlockPos(net.minecraft.world.entity.PathfinderMob $$0, Level $$1, BlockPos $$2) {
      Set<GlobalPos> $$3 = new HashSet<>(getVisitedPositions($$0));
      $$3.add(new GlobalPos($$1.dimension(), $$2));
      if ($$3.size() > 10) {
         this.enterCooldownAfterNoMatchingTargetFound($$0);
      } else {
         $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.VISITED_BLOCK_POSITIONS, $$3, 6000L);
      }
   }

   protected void markVisitedBlockPosAsUnreachable(net.minecraft.world.entity.PathfinderMob $$0, Level $$1, BlockPos $$2) {
      Set<GlobalPos> $$3 = new HashSet<>(getVisitedPositions($$0));
      $$3.remove(new GlobalPos($$1.dimension(), $$2));
      Set<GlobalPos> $$4 = new HashSet<>(getUnreachablePositions($$0));
      $$4.add(new GlobalPos($$1.dimension(), $$2));
      if ($$4.size() > 50) {
         this.enterCooldownAfterNoMatchingTargetFound($$0);
      } else {
         $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.VISITED_BLOCK_POSITIONS, $$3, 6000L);
         $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, $$4, 6000L);
      }
   }

   private boolean isWantedBlock(net.minecraft.world.entity.PathfinderMob $$0, BlockState $$1) {
      return isPickingUpItems($$0) ? this.sourceBlockType.test($$1) : this.destinationBlockType.test($$1);
   }

   private static double getInteractionRange(net.minecraft.world.entity.PathfinderMob $$0) {
      return hasFinishedPath($$0) ? 1.0 : 0.5;
   }

   private boolean isWithinTargetDistance(
      double $$0, TransportItemsBetweenContainers.TransportItemTarget $$1, Level $$2, net.minecraft.world.entity.PathfinderMob $$3, Vec3 $$4
   ) {
      AABB $$5 = $$3.getBoundingBox();
      AABB $$6 = AABB.ofSize($$4, $$5.getXsize(), $$5.getYsize(), $$5.getZsize());
      return $$1.state.getCollisionShape($$2, $$1.pos).bounds().inflate($$0, 0.5, $$0).move($$1.pos).intersects($$6);
   }

   private boolean targetIsReachableFromPosition(
      Level $$0, boolean $$1, Vec3 $$2, TransportItemsBetweenContainers.TransportItemTarget $$3, net.minecraft.world.entity.PathfinderMob $$4
   ) {
      return $$1 && this.canSeeAnyTargetSide($$3, $$0, $$4, $$2);
   }

   private boolean canSeeAnyTargetSide(
      TransportItemsBetweenContainers.TransportItemTarget $$0, Level $$1, net.minecraft.world.entity.PathfinderMob $$2, Vec3 $$3
   ) {
      Vec3 $$4 = $$0.pos.getCenter();
      return Direction.stream()
         .map($$1x -> $$4.add(0.5 * $$1x.getStepX(), 0.5 * $$1x.getStepY(), 0.5 * $$1x.getStepZ()))
         .map($$3x -> $$1.clip(new ClipContext($$3, $$3x, Block.COLLIDER, Fluid.NONE, $$2)))
         .anyMatch($$1x -> $$1x.getType() == Type.BLOCK && $$1x.getBlockPos().equals($$0.pos));
   }

   private boolean isAnotherMobInteractingWithTarget(TransportItemsBetweenContainers.TransportItemTarget $$0, Level $$1) {
      return this.getConnectedTargets($$0, $$1).anyMatch(this.shouldQueueForTarget);
   }

   private static boolean isPickingUpItems(net.minecraft.world.entity.PathfinderMob $$0) {
      return $$0.getMainHandItem().isEmpty();
   }

   private static boolean matchesGettingItemsRequirement(Container $$0) {
      return !$$0.isEmpty();
   }

   private static boolean matchesLeavingItemsRequirement(net.minecraft.world.entity.PathfinderMob $$0, Container $$1) {
      return $$1.isEmpty() || hasItemMatchingHandItem($$0, $$1);
   }

   private static boolean hasItemMatchingHandItem(net.minecraft.world.entity.PathfinderMob $$0, Container $$1) {
      ItemStack $$2 = $$0.getMainHandItem();

      for (ItemStack $$3 : $$1) {
         if (ItemStack.isSameItem($$3, $$2)) {
            return true;
         }
      }

      return false;
   }

   private void pickUpItems(net.minecraft.world.entity.PathfinderMob $$0, Container $$1) {
      $$0.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, pickupItemFromContainer($$1));
      $$0.setGuaranteedDrop(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
      $$1.setChanged();
      this.clearMemoriesAfterMatchingTargetFound($$0);
   }

   private void putDownItem(net.minecraft.world.entity.PathfinderMob $$0, Container $$1) {
      ItemStack $$2 = addItemsToContainer($$0, $$1);
      $$1.setChanged();
      $$0.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, $$2);
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

         $$1++;
      }

      return ItemStack.EMPTY;
   }

   private static ItemStack addItemsToContainer(net.minecraft.world.entity.PathfinderMob $$0, Container $$1) {
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

         $$2++;
      }

      return $$3;
   }

   protected void stopTargetingCurrentTarget(net.minecraft.world.entity.PathfinderMob $$0) {
      this.ticksSinceReachingTarget = 0;
      this.target = null;
      $$0.getNavigation().stop();
      $$0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
   }

   protected void clearMemoriesAfterMatchingTargetFound(net.minecraft.world.entity.PathfinderMob $$0) {
      this.stopTargetingCurrentTarget($$0);
      $$0.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
      $$0.getBrain().eraseMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
   }

   private void enterCooldownAfterNoMatchingTargetFound(net.minecraft.world.entity.PathfinderMob $$0) {
      this.stopTargetingCurrentTarget($$0);
      $$0.getBrain().setMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, 140);
      $$0.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
      $$0.getBrain().eraseMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
   }

   protected void stop(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      this.onStartTravelling($$1);
      if ($$1.getNavigation() instanceof GroundPathNavigation $$3) {
         $$3.setCanPathToTargetsBelowSurface(false);
      }
   }

   private void stopInPlace(net.minecraft.world.entity.PathfinderMob $$0) {
      $$0.getNavigation().stop();
      $$0.setXxa(0.0F);
      $$0.setYya(0.0F);
      $$0.setSpeed(0.0F);
      $$0.setDeltaMovement(0.0, $$0.getDeltaMovement().y, 0.0);
   }

   public static enum ContainerInteractionState {
      PICKUP_ITEM,
      PICKUP_NO_ITEM,
      PLACE_ITEM,
      PLACE_NO_ITEM;
   }

   @FunctionalInterface
   public interface OnTargetReachedInteraction
      extends TriConsumer<net.minecraft.world.entity.PathfinderMob, TransportItemsBetweenContainers.TransportItemTarget, Integer> {
   }

   public static enum TransportItemState {
      TRAVELLING,
      QUEUING,
      INTERACTING;
   }

   public record TransportItemTarget(BlockPos pos, Container container, BlockEntity blockEntity, BlockState state) {

      @Nullable
      public static TransportItemsBetweenContainers.TransportItemTarget tryCreatePossibleTarget(BlockEntity $$0, Level $$1) {
         BlockPos $$2 = $$0.getBlockPos();
         BlockState $$3 = $$0.getBlockState();
         Container $$4 = getBlockEntityContainer($$0, $$3, $$1, $$2);
         return $$4 != null ? new TransportItemsBetweenContainers.TransportItemTarget($$2, $$4, $$0, $$3) : null;
      }

      @Nullable
      public static TransportItemsBetweenContainers.TransportItemTarget tryCreatePossibleTarget(BlockPos $$0, Level $$1) {
         BlockEntity $$2 = $$1.getBlockEntity($$0);
         return $$2 == null ? null : tryCreatePossibleTarget($$2, $$1);
      }

      @Nullable
      private static Container getBlockEntityContainer(BlockEntity $$0, BlockState $$1, Level $$2, BlockPos $$3) {
         if ($$1.getBlock() instanceof ChestBlock $$4) {
            return ChestBlock.getContainer($$4, $$1, $$2, $$3, false);
         } else {
            return $$0 instanceof Container $$5 ? $$5 : null;
         }
      }
   }
}
