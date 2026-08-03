package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.phys.Vec3;

public class LeapAtTargetGoal extends Goal {
   private final net.minecraft.world.entity.Mob mob;
   private net.minecraft.world.entity.LivingEntity target;
   private final float yd;

   public LeapAtTargetGoal(net.minecraft.world.entity.Mob $$0, float $$1) {
      this.mob = $$0;
      this.yd = $$1;
      this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.mob.hasControllingPassenger()) {
         return false;
      } else {
         this.target = this.mob.getTarget();
         if (this.target == null) {
            return false;
         } else {
            double $$0 = this.mob.distanceToSqr(this.target);
            if ($$0 < 4.0 || $$0 > 16.0) {
               return false;
            } else {
               return !this.mob.onGround() ? false : this.mob.getRandom().nextInt(reducedTickDelay(5)) == 0;
            }
         }
      }
   }

   @Override
   public boolean canContinueToUse() {
      return !this.mob.onGround();
   }

   @Override
   public void start() {
      Vec3 $$0 = this.mob.getDeltaMovement();
      Vec3 $$1 = new Vec3(this.target.getX() - this.mob.getX(), 0.0, this.target.getZ() - this.mob.getZ());
      if ($$1.lengthSqr() > 1.0E-7) {
         $$1 = $$1.normalize().scale(0.4).add($$0.scale(0.2));
      }

      this.mob.setDeltaMovement($$1.x, this.yd, $$1.z);
   }
}
