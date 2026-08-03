package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MoveToTargetSink extends Behavior<net.minecraft.world.entity.Mob> {
   private static final int MAX_COOLDOWN_BEFORE_RETRYING = 40;
   private int remainingCooldown;
   @Nullable
   private Path path;
   @Nullable
   private BlockPos lastTargetPos;
   private float speedModifier;

   public MoveToTargetSink() {
      this(150, 250);
   }

   public MoveToTargetSink(int $$0, int $$1) {
      super(
         ImmutableMap.of(
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryStatus.REGISTERED,
            MemoryModuleType.PATH,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.VALUE_PRESENT
         ),
         $$0,
         $$1
      );
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, net.minecraft.world.entity.Mob $$1) {
      if (this.remainingCooldown > 0) {
         this.remainingCooldown--;
         return false;
      } else {
         Brain<?> $$2 = $$1.getBrain();
         WalkTarget $$3 = $$2.getMemory(MemoryModuleType.WALK_TARGET).get();
         boolean $$4 = this.reachedTarget($$1, $$3);
         if (!$$4 && this.tryComputePath($$1, $$3, $$0.getGameTime())) {
            this.lastTargetPos = $$3.getTarget().currentBlockPosition();
            return true;
         } else {
            $$2.eraseMemory(MemoryModuleType.WALK_TARGET);
            if ($$4) {
               $$2.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            }

            return false;
         }
      }
   }

   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      if (this.path != null && this.lastTargetPos != null) {
         Optional<WalkTarget> $$3 = $$1.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
         boolean $$4 = $$3.map(MoveToTargetSink::isWalkTargetSpectator).orElse(false);
         PathNavigation $$5 = $$1.getNavigation();
         return !$$5.isDone() && $$3.isPresent() && !this.reachedTarget($$1, $$3.get()) && !$$4;
      } else {
         return false;
      }
   }

   protected void stop(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      if ($$1.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)
         && !this.reachedTarget($$1, $$1.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get())
         && $$1.getNavigation().isStuck()) {
         this.remainingCooldown = $$0.getRandom().nextInt(40);
      }

      $$1.getNavigation().stop();
      $$1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      $$1.getBrain().eraseMemory(MemoryModuleType.PATH);
      this.path = null;
   }

   protected void start(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      $$1.getBrain().setMemory(MemoryModuleType.PATH, this.path);
      $$1.getNavigation().moveTo(this.path, (double)this.speedModifier);
   }

   protected void tick(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      Path $$3 = $$1.getNavigation().getPath();
      Brain<?> $$4 = $$1.getBrain();
      if (this.path != $$3) {
         this.path = $$3;
         $$4.setMemory(MemoryModuleType.PATH, $$3);
      }

      if ($$3 != null && this.lastTargetPos != null) {
         WalkTarget $$5 = $$4.getMemory(MemoryModuleType.WALK_TARGET).get();
         if ($$5.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0 && this.tryComputePath($$1, $$5, $$0.getGameTime())) {
            this.lastTargetPos = $$5.getTarget().currentBlockPosition();
            this.start($$0, $$1, $$2);
         }
      }
   }

   private boolean tryComputePath(net.minecraft.world.entity.Mob $$0, WalkTarget $$1, long $$2) {
      BlockPos $$3 = $$1.getTarget().currentBlockPosition();
      this.path = $$0.getNavigation().createPath($$3, 0);
      this.speedModifier = $$1.getSpeedModifier();
      Brain<?> $$4 = $$0.getBrain();
      if (this.reachedTarget($$0, $$1)) {
         $$4.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      } else {
         boolean $$5 = this.path != null && this.path.canReach();
         if ($$5) {
            $$4.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         } else if (!$$4.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
            $$4.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, $$2);
         }

         if (this.path != null) {
            return true;
         }

         Vec3 $$6 = DefaultRandomPos.getPosTowards((net.minecraft.world.entity.PathfinderMob)$$0, 10, 7, Vec3.atBottomCenterOf($$3), (float) (Math.PI / 2));
         if ($$6 != null) {
            this.path = $$0.getNavigation().createPath($$6.x, $$6.y, $$6.z, 0);
            return this.path != null;
         }
      }

      return false;
   }

   private boolean reachedTarget(net.minecraft.world.entity.Mob $$0, WalkTarget $$1) {
      return $$1.getTarget().currentBlockPosition().distManhattan($$0.blockPosition()) <= $$1.getCloseEnoughDist();
   }

   private static boolean isWalkTargetSpectator(WalkTarget $$0) {
      return $$0.getTarget() instanceof EntityTracker $$2 ? $$2.getEntity().isSpectator() : false;
   }
}
