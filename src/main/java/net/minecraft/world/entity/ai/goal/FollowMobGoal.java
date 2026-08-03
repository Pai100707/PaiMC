package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

public class FollowMobGoal extends Goal {
   private final net.minecraft.world.entity.Mob mob;
   private final Predicate<net.minecraft.world.entity.Mob> followPredicate;
   @Nullable
   private net.minecraft.world.entity.Mob followingMob;
   private final double speedModifier;
   private final PathNavigation navigation;
   private int timeToRecalcPath;
   private final float stopDistance;
   private float oldWaterCost;
   private final float areaSize;

   public FollowMobGoal(net.minecraft.world.entity.Mob $$0, double $$1, float $$2, float $$3) {
      this.mob = $$0;
      this.followPredicate = $$1x -> $$0.getClass() != $$1x.getClass();
      this.speedModifier = $$1;
      this.navigation = $$0.getNavigation();
      this.stopDistance = $$2;
      this.areaSize = $$3;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      if (!($$0.getNavigation() instanceof GroundPathNavigation) && !($$0.getNavigation() instanceof FlyingPathNavigation)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
      }
   }

   @Override
   public boolean canUse() {
      List<net.minecraft.world.entity.Mob> $$0 = this.mob
         .level()
         .getEntitiesOfClass(net.minecraft.world.entity.Mob.class, this.mob.getBoundingBox().inflate(this.areaSize), this.followPredicate);
      if (!$$0.isEmpty()) {
         for (net.minecraft.world.entity.Mob $$1 : $$0) {
            if (!$$1.isInvisible()) {
               this.followingMob = $$1;
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean canContinueToUse() {
      return this.followingMob != null && !this.navigation.isDone() && this.mob.distanceToSqr(this.followingMob) > this.stopDistance * this.stopDistance;
   }

   @Override
   public void start() {
      this.timeToRecalcPath = 0;
      this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER);
      this.mob.setPathfindingMalus(PathType.WATER, 0.0F);
   }

   @Override
   public void stop() {
      this.followingMob = null;
      this.navigation.stop();
      this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
   }

   @Override
   public void tick() {
      if (this.followingMob != null && !this.mob.isLeashed()) {
         this.mob.getLookControl().setLookAt(this.followingMob, 10.0F, this.mob.getMaxHeadXRot());
         if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            double $$0 = this.mob.getX() - this.followingMob.getX();
            double $$1 = this.mob.getY() - this.followingMob.getY();
            double $$2 = this.mob.getZ() - this.followingMob.getZ();
            double $$3 = $$0 * $$0 + $$1 * $$1 + $$2 * $$2;
            if (!($$3 <= this.stopDistance * this.stopDistance)) {
               this.navigation.moveTo(this.followingMob, this.speedModifier);
            } else {
               this.navigation.stop();
               LookControl $$4 = this.followingMob.getLookControl();
               if ($$3 <= this.stopDistance
                  || $$4.getWantedX() == this.mob.getX() && $$4.getWantedY() == this.mob.getY() && $$4.getWantedZ() == this.mob.getZ()) {
                  double $$5 = this.followingMob.getX() - this.mob.getX();
                  double $$6 = this.followingMob.getZ() - this.mob.getZ();
                  this.navigation.moveTo(this.mob.getX() - $$5, this.mob.getY(), this.mob.getZ() - $$6, this.speedModifier);
               }
            }
         }
      }
   }
}
