/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class PrepareRamNearestTarget<E extends PathfinderMob>
extends Behavior<E> {
    public static final int TIME_OUT_DURATION = 160;
    private final ToIntFunction<E> getCooldownOnFail;
    private final int minRamDistance;
    private final int maxRamDistance;
    private final float walkSpeed;
    private final TargetingConditions ramTargeting;
    private final int ramPrepareTime;
    private final Function<E, SoundEvent> getPrepareRamSound;
    private Optional<Long> reachedRamPositionTimestamp = Optional.empty();
    private Optional<RamCandidate> ramCandidate = Optional.empty();

    public PrepareRamNearestTarget(ToIntFunction<E> $$0, int $$1, int $$2, float $$3, TargetingConditions $$4, int $$5, Function<E, SoundEvent> $$6) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.RAM_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.RAM_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), 160);
        this.getCooldownOnFail = $$0;
        this.minRamDistance = $$1;
        this.maxRamDistance = $$2;
        this.walkSpeed = $$3;
        this.ramTargeting = $$4;
        this.ramPrepareTime = $$5;
        this.getPrepareRamSound = $$6;
    }

    @Override
    protected void start(ServerLevel $$0, PathfinderMob $$12, long $$2) {
        Brain<?> $$3 = $$12.getBrain();
        $$3.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).flatMap($$22 -> $$22.findClosest($$2 -> this.ramTargeting.test($$0, $$12, (LivingEntity)$$2))).ifPresent($$1 -> this.chooseRamPosition($$12, (LivingEntity)$$1));
    }

    @Override
    protected void stop(ServerLevel $$0, E $$1, long $$2) {
        Brain<Vec3> $$3 = ((LivingEntity)$$1).getBrain();
        if (!$$3.hasMemoryValue(MemoryModuleType.RAM_TARGET)) {
            $$0.broadcastEntityEvent((Entity)$$1, (byte)59);
            $$3.setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, this.getCooldownOnFail.applyAsInt($$1));
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        return this.ramCandidate.isPresent() && this.ramCandidate.get().getTarget().isAlive();
    }

    @Override
    protected void tick(ServerLevel $$0, E $$1, long $$2) {
        boolean $$3;
        if (this.ramCandidate.isEmpty()) {
            return;
        }
        ((LivingEntity)$$1).getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.ramCandidate.get().getStartPosition(), this.walkSpeed, 0));
        ((LivingEntity)$$1).getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.ramCandidate.get().getTarget(), true));
        boolean bl = $$3 = !this.ramCandidate.get().getTarget().blockPosition().equals(this.ramCandidate.get().getTargetPosition());
        if ($$3) {
            $$0.broadcastEntityEvent((Entity)$$1, (byte)59);
            ((Mob)$$1).getNavigation().stop();
            this.chooseRamPosition((PathfinderMob)$$1, this.ramCandidate.get().target);
        } else {
            BlockPos $$4 = ((Entity)$$1).blockPosition();
            if ($$4.equals(this.ramCandidate.get().getStartPosition())) {
                $$0.broadcastEntityEvent((Entity)$$1, (byte)58);
                if (this.reachedRamPositionTimestamp.isEmpty()) {
                    this.reachedRamPositionTimestamp = Optional.of($$2);
                }
                if ($$2 - this.reachedRamPositionTimestamp.get() >= (long)this.ramPrepareTime) {
                    ((LivingEntity)$$1).getBrain().setMemory(MemoryModuleType.RAM_TARGET, this.getEdgeOfBlock($$4, this.ramCandidate.get().getTargetPosition()));
                    $$0.playSound(null, (Entity)$$1, this.getPrepareRamSound.apply($$1), SoundSource.NEUTRAL, 1.0f, ((LivingEntity)$$1).getVoicePitch());
                    this.ramCandidate = Optional.empty();
                }
            }
        }
    }

    private Vec3 getEdgeOfBlock(BlockPos $$0, BlockPos $$1) {
        double $$2 = 0.5;
        double $$3 = 0.5 * (double)Mth.sign($$1.getX() - $$0.getX());
        double $$4 = 0.5 * (double)Mth.sign($$1.getZ() - $$0.getZ());
        return Vec3.atBottomCenterOf($$1).add($$3, 0.0, $$4);
    }

    private Optional<BlockPos> calculateRammingStartPosition(PathfinderMob $$0, LivingEntity $$12) {
        BlockPos $$2 = $$12.blockPosition();
        if (!this.isWalkableBlock($$0, $$2)) {
            return Optional.empty();
        }
        ArrayList $$3 = Lists.newArrayList();
        BlockPos.MutableBlockPos $$4 = $$2.mutable();
        for (Direction $$5 : Direction.Plane.HORIZONTAL) {
            $$4.set($$2);
            for (int $$6 = 0; $$6 < this.maxRamDistance; ++$$6) {
                if (this.isWalkableBlock($$0, $$4.move($$5))) continue;
                $$4.move($$5.getOpposite());
                break;
            }
            if ($$4.distManhattan($$2) < this.minRamDistance) continue;
            $$3.add($$4.immutable());
        }
        PathNavigation $$7 = $$0.getNavigation();
        return $$3.stream().sorted(Comparator.comparingDouble($$0.blockPosition()::distSqr)).filter($$1 -> {
            Path $$2 = $$7.createPath((BlockPos)$$1, 0);
            return $$2 != null && $$2.canReach();
        }).findFirst();
    }

    private boolean isWalkableBlock(PathfinderMob $$0, BlockPos $$1) {
        return $$0.getNavigation().isStableDestination($$1) && $$0.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic($$0, $$1)) == 0.0f;
    }

    private void chooseRamPosition(PathfinderMob $$0, LivingEntity $$12) {
        this.reachedRamPositionTimestamp = Optional.empty();
        this.ramCandidate = this.calculateRammingStartPosition($$0, $$12).map($$1 -> new RamCandidate((BlockPos)$$1, $$12.blockPosition(), $$12));
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }

    public static class RamCandidate {
        private final BlockPos startPosition;
        private final BlockPos targetPosition;
        final LivingEntity target;

        public RamCandidate(BlockPos $$0, BlockPos $$1, LivingEntity $$2) {
            this.startPosition = $$0;
            this.targetPosition = $$1;
            this.target = $$2;
        }

        public BlockPos getStartPosition() {
            return this.startPosition;
        }

        public BlockPos getTargetPosition() {
            return this.targetPosition;
        }

        public LivingEntity getTarget() {
            return this.target;
        }
    }
}

