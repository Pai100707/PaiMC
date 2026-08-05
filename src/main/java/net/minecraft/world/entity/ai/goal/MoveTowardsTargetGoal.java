package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveTowardsTargetGoal extends Goal {
   private final net.minecraft.world.entity.PathfinderMob mob;
   
   private net.minecraft.world.entity.LivingEntity target;
   private double wantedX;
   private double wantedY;
   private double wantedZ;
   private final double speedModifier;
   private final float within;

   public MoveTowardsTargetGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, float $$2) {
      this.mob = $$0;
      this.speedModifier = $$1;
      this.within = $$2;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      this.target = this.mob.getTarget();
      if (this.target == null) {
         return false;
      } else if (this.target.distanceToSqr(this.mob) > this.within * this.within) {
         return false;
      } else {
         Vec3 $$0 = DefaultRandomPos.getPosTowards(this.mob, 16, 7, this.target.position(), (float) (Math.PI / 2));
         if ($$0 == null) {
            return false;
         } else {
            this.wantedX = $$0.x;
            this.wantedY = $$0.y;
            this.wantedZ = $$0.z;
            return true;
         }
      }
   }

   @Override
   public boolean canContinueToUse() {
      return !this.mob.getNavigation().isDone() && this.target.isAlive() && this.target.distanceToSqr(this.mob) < this.within * this.within;
   }

   @Override
   public void stop() {
      this.target = null;
   }

   @Override
   public void start() {
      this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
   }
}
