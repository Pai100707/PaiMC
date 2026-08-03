package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class PrepareRamNearestTarget<E extends net.minecraft.world.entity.PathfinderMob> extends Behavior<E> {
   public static final int TIME_OUT_DURATION = 160;
   private final ToIntFunction<E> getCooldownOnFail;
   private final int minRamDistance;
   private final int maxRamDistance;
   private final float walkSpeed;
   private final TargetingConditions ramTargeting;
   private final int ramPrepareTime;
   private final Function<E, SoundEvent> getPrepareRamSound;
   private Optional<Long> reachedRamPositionTimestamp = Optional.empty();
   private Optional<PrepareRamNearestTarget.RamCandidate> ramCandidate = Optional.empty();

   public PrepareRamNearestTarget(ToIntFunction<E> $$0, int $$1, int $$2, float $$3, TargetingConditions $$4, int $$5, Function<E, SoundEvent> $$6) {
      super(
         ImmutableMap.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.RAM_COOLDOWN_TICKS,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.RAM_TARGET,
            MemoryStatus.VALUE_ABSENT
         ),
         160
      );
      this.getCooldownOnFail = $$0;
      this.minRamDistance = $$1;
      this.maxRamDistance = $$2;
      this.walkSpeed = $$3;
      this.ramTargeting = $$4;
      this.ramPrepareTime = $$5;
      this.getPrepareRamSound = $$6;
   }

   protected void start(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      Brain<?> $$3 = $$1.getBrain();
      $$3.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
         .flatMap($$2x -> $$2x.findClosest($$2xx -> this.ramTargeting.test($$0, $$1, $$2xx)))
         .ifPresent($$1x -> this.chooseRamPosition($$1, $$1x));
   }

   protected void stop(ServerLevel $$0, E $$1, long $$2) {
      Brain<?> $$3 = $$1.getBrain();
      if (!$$3.hasMemoryValue(MemoryModuleType.RAM_TARGET)) {
         $$0.broadcastEntityEvent($$1, (byte)59);
         $$3.setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, this.getCooldownOnFail.applyAsInt($$1));
      }
   }

   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      return this.ramCandidate.isPresent() && this.ramCandidate.get().getTarget().isAlive();
   }

   protected void tick(ServerLevel $$0, E $$1, long $$2) {
      if (!this.ramCandidate.isEmpty()) {
         $$1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.ramCandidate.get().getStartPosition(), this.walkSpeed, 0));
         $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.ramCandidate.get().getTarget(), true));
         boolean $$3 = !this.ramCandidate.get().getTarget().blockPosition().equals(this.ramCandidate.get().getTargetPosition());
         if ($$3) {
            $$0.broadcastEntityEvent($$1, (byte)59);
            $$1.getNavigation().stop();
            this.chooseRamPosition($$1, this.ramCandidate.get().target);
         } else {
            BlockPos $$4 = $$1.blockPosition();
            if ($$4.equals(this.ramCandidate.get().getStartPosition())) {
               $$0.broadcastEntityEvent($$1, (byte)58);
               if (this.reachedRamPositionTimestamp.isEmpty()) {
                  this.reachedRamPositionTimestamp = Optional.of($$2);
               }

               if ($$2 - this.reachedRamPositionTimestamp.get() >= this.ramPrepareTime) {
                  $$1.getBrain().setMemory(MemoryModuleType.RAM_TARGET, this.getEdgeOfBlock($$4, this.ramCandidate.get().getTargetPosition()));
                  $$0.playSound(null, $$1, this.getPrepareRamSound.apply($$1), SoundSource.NEUTRAL, 1.0F, $$1.getVoicePitch());
                  this.ramCandidate = Optional.empty();
               }
            }
         }
      }
   }

   private Vec3 getEdgeOfBlock(BlockPos $$0, BlockPos $$1) {
      double $$2 = 0.5;
      double $$3 = 0.5 * Mth.sign($$1.getX() - $$0.getX());
      double $$4 = 0.5 * Mth.sign($$1.getZ() - $$0.getZ());
      return Vec3.atBottomCenterOf($$1).add($$3, 0.0, $$4);
   }

   private Optional<BlockPos> calculateRammingStartPosition(net.minecraft.world.entity.PathfinderMob $$0, net.minecraft.world.entity.LivingEntity $$1) {
      BlockPos $$2 = $$1.blockPosition();
      if (!this.isWalkableBlock($$0, $$2)) {
         return Optional.empty();
      } else {
         List<BlockPos> $$3 = Lists.newArrayList();
         MutableBlockPos $$4 = $$2.mutable();

         for (Direction $$5 : Plane.HORIZONTAL) {
            $$4.set($$2);

            for (int $$6 = 0; $$6 < this.maxRamDistance; $$6++) {
               if (!this.isWalkableBlock($$0, $$4.move($$5))) {
                  $$4.move($$5.getOpposite());
                  break;
               }
            }

            if ($$4.distManhattan($$2) >= this.minRamDistance) {
               $$3.add($$4.immutable());
            }
         }

         PathNavigation $$7 = $$0.getNavigation();
         return $$3.stream().sorted(Comparator.comparingDouble($$0.blockPosition()::distSqr)).filter($$1x -> {
            Path $$2x = $$7.createPath($$1x, 0);
            return $$2x != null && $$2x.canReach();
         }).findFirst();
      }
   }

   private boolean isWalkableBlock(net.minecraft.world.entity.PathfinderMob $$0, BlockPos $$1) {
      return $$0.getNavigation().isStableDestination($$1) && $$0.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic($$0, $$1)) == 0.0F;
   }

   private void chooseRamPosition(net.minecraft.world.entity.PathfinderMob $$0, net.minecraft.world.entity.LivingEntity $$1) {
      this.reachedRamPositionTimestamp = Optional.empty();
      this.ramCandidate = this.calculateRammingStartPosition($$0, $$1).map($$1x -> new PrepareRamNearestTarget.RamCandidate($$1x, $$1.blockPosition(), $$1));
   }

   public static class RamCandidate {
      private final BlockPos startPosition;
      private final BlockPos targetPosition;
      final net.minecraft.world.entity.LivingEntity target;

      public RamCandidate(BlockPos $$0, BlockPos $$1, net.minecraft.world.entity.LivingEntity $$2) {
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

      public net.minecraft.world.entity.LivingEntity getTarget() {
         return this.target;
      }
   }
}
