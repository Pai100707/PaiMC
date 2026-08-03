package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;

public class OcelotAttackGoal extends Goal {
   private final net.minecraft.world.entity.Mob mob;
   private net.minecraft.world.entity.LivingEntity target;
   private int attackTime;

   public OcelotAttackGoal(net.minecraft.world.entity.Mob $$0) {
      this.mob = $$0;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      net.minecraft.world.entity.LivingEntity $$0 = this.mob.getTarget();
      if ($$0 == null) {
         return false;
      } else {
         this.target = $$0;
         return true;
      }
   }

   @Override
   public boolean canContinueToUse() {
      if (!this.target.isAlive()) {
         return false;
      } else {
         return this.mob.distanceToSqr(this.target) > 225.0 ? false : !this.mob.getNavigation().isDone() || this.canUse();
      }
   }

   @Override
   public void stop() {
      this.target = null;
      this.mob.getNavigation().stop();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
      double $$0 = this.mob.getBbWidth() * 2.0F * (this.mob.getBbWidth() * 2.0F);
      double $$1 = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
      double $$2 = 0.8;
      if ($$1 > $$0 && $$1 < 16.0) {
         $$2 = 1.33;
      } else if ($$1 < 225.0) {
         $$2 = 0.6;
      }

      this.mob.getNavigation().moveTo(this.target, $$2);
      this.attackTime = Math.max(this.attackTime - 1, 0);
      if (!($$1 > $$0)) {
         if (this.attackTime <= 0) {
            this.attackTime = 20;
            this.mob.doHurtTarget(getServerLevel(this.mob), this.target);
         }
      }
   }
}
