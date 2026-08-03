package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

public abstract class PathfinderMob extends net.minecraft.world.entity.Mob {
   protected static final float DEFAULT_WALK_TARGET_VALUE = 0.0F;

   protected PathfinderMob(net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.PathfinderMob> $$0, Level $$1) {
      super($$0, $$1);
   }

   public float getWalkTargetValue(BlockPos $$0) {
      return this.getWalkTargetValue($$0, this.level());
   }

   public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
      return 0.0F;
   }

   @Override
   public boolean checkSpawnRules(LevelAccessor $$0, net.minecraft.world.entity.EntitySpawnReason $$1) {
      return this.getWalkTargetValue(this.blockPosition(), $$0) >= 0.0F;
   }

   public boolean isPathFinding() {
      return !this.getNavigation().isDone();
   }

   public boolean isPanicking() {
      if (this.brain.hasMemoryValue(MemoryModuleType.IS_PANICKING)) {
         return this.brain.getMemory(MemoryModuleType.IS_PANICKING).isPresent();
      } else {
         for (WrappedGoal $$0 : this.goalSelector.getAvailableGoals()) {
            if ($$0.isRunning() && $$0.getGoal() instanceof PanicGoal) {
               return true;
            }
         }

         return false;
      }
   }

   protected boolean shouldStayCloseToLeashHolder() {
      return true;
   }

   @Override
   public void closeRangeLeashBehaviour(net.minecraft.world.entity.Entity $$0) {
      super.closeRangeLeashBehaviour($$0);
      if (this.shouldStayCloseToLeashHolder() && !this.isPanicking()) {
         this.goalSelector.enableControlFlag(Goal.Flag.MOVE);
         float $$1 = 2.0F;
         float $$2 = this.distanceTo($$0);
         Vec3 $$3 = new Vec3($$0.getX() - this.getX(), $$0.getY() - this.getY(), $$0.getZ() - this.getZ()).normalize().scale(Math.max($$2 - 2.0F, 0.0F));
         this.getNavigation().moveTo(this.getX() + $$3.x, this.getY() + $$3.y, this.getZ() + $$3.z, this.followLeashSpeed());
      }
   }

   @Override
   public void whenLeashedTo(net.minecraft.world.entity.Entity $$0) {
      this.setHomeTo($$0.blockPosition(), (int)this.leashElasticDistance() - 1);
      super.whenLeashedTo($$0);
   }

   protected double followLeashSpeed() {
      return 1.0;
   }
}
