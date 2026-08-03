package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FollowBoatGoal extends Goal {
   private int timeToRecalcPath;
   private final net.minecraft.world.entity.PathfinderMob mob;
   @Nullable
   private Player following;
   private BoatGoals currentGoal;

   public FollowBoatGoal(net.minecraft.world.entity.PathfinderMob $$0) {
      this.mob = $$0;
   }

   @Override
   public boolean canUse() {
      if (this.following != null && this.following.hasMovedHorizontallyRecently()) {
         return true;
      } else {
         for (AbstractBoat $$1 : this.mob.level().getEntitiesOfClass(AbstractBoat.class, this.mob.getBoundingBox().inflate(5.0))) {
            if ($$1.getControllingPassenger() instanceof Player $$2 && $$2.hasMovedHorizontallyRecently()) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean isInterruptable() {
      return true;
   }

   @Override
   public boolean canContinueToUse() {
      return this.following != null && this.following.isPassenger() && this.following.hasMovedHorizontallyRecently();
   }

   @Override
   public void start() {
      for (AbstractBoat $$1 : this.mob.level().getEntitiesOfClass(AbstractBoat.class, this.mob.getBoundingBox().inflate(5.0))) {
         if ($$1.getControllingPassenger() instanceof Player $$2) {
            this.following = $$2;
            break;
         }
      }

      this.timeToRecalcPath = 0;
      this.currentGoal = BoatGoals.GO_TO_BOAT;
   }

   @Override
   public void stop() {
      this.following = null;
   }

   @Override
   public void tick() {
      float $$0 = this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION ? 0.01F : 0.015F;
      this.mob.moveRelative($$0, new Vec3(this.mob.xxa, this.mob.yya, this.mob.zza));
      this.mob.move(net.minecraft.world.entity.MoverType.SELF, this.mob.getDeltaMovement());
      if (--this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = this.adjustedTickDelay(10);
         if (this.currentGoal == BoatGoals.GO_TO_BOAT) {
            BlockPos $$1 = this.following.blockPosition().relative(this.following.getDirection().getOpposite());
            $$1 = $$1.offset(0, -1, 0);
            this.mob.getNavigation().moveTo($$1.getX(), $$1.getY(), $$1.getZ(), 1.0);
            if (this.mob.distanceTo(this.following) < 4.0F) {
               this.timeToRecalcPath = 0;
               this.currentGoal = BoatGoals.GO_IN_BOAT_DIRECTION;
            }
         } else if (this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION) {
            Direction $$2 = this.following.getMotionDirection();
            BlockPos $$3 = this.following.blockPosition().relative($$2, 10);
            this.mob.getNavigation().moveTo($$3.getX(), $$3.getY() - 1, $$3.getZ(), 1.0);
            if (this.mob.distanceTo(this.following) > 12.0F) {
               this.timeToRecalcPath = 0;
               this.currentGoal = BoatGoals.GO_TO_BOAT;
            }
         }
      }
   }
}
