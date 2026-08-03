package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;

public class SitWhenOrderedToGoal extends Goal {
   private final net.minecraft.world.entity.TamableAnimal mob;

   public SitWhenOrderedToGoal(net.minecraft.world.entity.TamableAnimal $$0) {
      this.mob = $$0;
      this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
   }

   @Override
   public boolean canContinueToUse() {
      return this.mob.isOrderedToSit();
   }

   @Override
   public boolean canUse() {
      boolean $$0 = this.mob.isOrderedToSit();
      if (!$$0 && !this.mob.isTame()) {
         return false;
      } else if (this.mob.isInWater()) {
         return false;
      } else if (!this.mob.onGround()) {
         return false;
      } else {
         net.minecraft.world.entity.LivingEntity $$1 = this.mob.getOwner();
         if ($$1 == null || $$1.level() != this.mob.level()) {
            return true;
         } else {
            return this.mob.distanceToSqr($$1) < 144.0 && $$1.getLastHurtByMob() != null ? false : $$0;
         }
      }
   }

   @Override
   public void start() {
      this.mob.getNavigation().stop();
      this.mob.setInSittingPose(true);
   }

   @Override
   public void stop() {
      this.mob.setInSittingPose(false);
   }
}
