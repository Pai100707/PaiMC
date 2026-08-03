package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;

public class SmoothSwimmingLookControl extends LookControl {
   private final int maxYRotFromCenter;
   private static final int HEAD_TILT_X = 10;
   private static final int HEAD_TILT_Y = 20;

   public SmoothSwimmingLookControl(net.minecraft.world.entity.Mob $$0, int $$1) {
      super($$0);
      this.maxYRotFromCenter = $$1;
   }

   @Override
   public void tick() {
      if (this.lookAtCooldown > 0) {
         this.lookAtCooldown--;
         this.getYRotD().ifPresent($$0x -> this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, $$0x + 20.0F, this.yMaxRotSpeed));
         this.getXRotD().ifPresent($$0x -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), $$0x + 10.0F, this.xMaxRotAngle)));
      } else {
         if (this.mob.getNavigation().isDone()) {
            this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), 0.0F, 5.0F));
         }

         this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, this.yMaxRotSpeed);
      }

      float $$0 = Mth.wrapDegrees(this.mob.yHeadRot - this.mob.yBodyRot);
      if ($$0 < -this.maxYRotFromCenter) {
         this.mob.yBodyRot -= 4.0F;
      } else if ($$0 > this.maxYRotFromCenter) {
         this.mob.yBodyRot += 4.0F;
      }
   }
}
