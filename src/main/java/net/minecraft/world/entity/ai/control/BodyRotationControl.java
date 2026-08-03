package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;

public class BodyRotationControl implements Control {
   private final net.minecraft.world.entity.Mob mob;
   private static final int HEAD_STABLE_ANGLE = 15;
   private static final int DELAY_UNTIL_STARTING_TO_FACE_FORWARD = 10;
   private static final int HOW_LONG_IT_TAKES_TO_FACE_FORWARD = 10;
   private int headStableTime;
   private float lastStableYHeadRot;

   public BodyRotationControl(net.minecraft.world.entity.Mob $$0) {
      this.mob = $$0;
   }

   public void clientTick() {
      if (this.isMoving()) {
         this.mob.yBodyRot = this.mob.getYRot();
         this.rotateHeadIfNecessary();
         this.lastStableYHeadRot = this.mob.yHeadRot;
         this.headStableTime = 0;
      } else {
         if (this.notCarryingMobPassengers()) {
            if (Math.abs(this.mob.yHeadRot - this.lastStableYHeadRot) > 15.0F) {
               this.headStableTime = 0;
               this.lastStableYHeadRot = this.mob.yHeadRot;
               this.rotateBodyIfNecessary();
            } else {
               this.headStableTime++;
               if (this.headStableTime > 10) {
                  this.rotateHeadTowardsFront();
               }
            }
         }
      }
   }

   private void rotateBodyIfNecessary() {
      this.mob.yBodyRot = Mth.rotateIfNecessary(this.mob.yBodyRot, this.mob.yHeadRot, this.mob.getMaxHeadYRot());
   }

   private void rotateHeadIfNecessary() {
      this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, this.mob.getMaxHeadYRot());
   }

   private void rotateHeadTowardsFront() {
      int $$0 = this.headStableTime - 10;
      float $$1 = Mth.clamp($$0 / 10.0F, 0.0F, 1.0F);
      float $$2 = this.mob.getMaxHeadYRot() * (1.0F - $$1);
      this.mob.yBodyRot = Mth.rotateIfNecessary(this.mob.yBodyRot, this.mob.yHeadRot, $$2);
   }

   private boolean notCarryingMobPassengers() {
      return !(this.mob.getFirstPassenger() instanceof net.minecraft.world.entity.Mob);
   }

   private boolean isMoving() {
      double $$0 = this.mob.getX() - this.mob.xo;
      double $$1 = this.mob.getZ() - this.mob.zo;
      return $$0 * $$0 + $$1 * $$1 > 2.5000003E-7F;
   }
}
