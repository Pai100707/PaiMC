package net.minecraft.world.entity.ai.control;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LookControl implements Control {
   protected final net.minecraft.world.entity.Mob mob;
   protected float yMaxRotSpeed;
   protected float xMaxRotAngle;
   protected int lookAtCooldown;
   protected double wantedX;
   protected double wantedY;
   protected double wantedZ;

   public LookControl(net.minecraft.world.entity.Mob $$0) {
      this.mob = $$0;
   }

   public void setLookAt(Vec3 $$0) {
      this.setLookAt($$0.x, $$0.y, $$0.z);
   }

   public void setLookAt(net.minecraft.world.entity.Entity $$0) {
      this.setLookAt($$0.getX(), $$0.getEyeY(), $$0.getZ());
   }

   public void setLookAt(net.minecraft.world.entity.Entity $$0, float $$1, float $$2) {
      this.setLookAt($$0.getX(), $$0.getEyeY(), $$0.getZ(), $$1, $$2);
   }

   public void setLookAt(double $$0, double $$1, double $$2) {
      this.setLookAt($$0, $$1, $$2, this.mob.getHeadRotSpeed(), this.mob.getMaxHeadXRot());
   }

   public void setLookAt(double $$0, double $$1, double $$2, float $$3, float $$4) {
      this.wantedX = $$0;
      this.wantedY = $$1;
      this.wantedZ = $$2;
      this.yMaxRotSpeed = $$3;
      this.xMaxRotAngle = $$4;
      this.lookAtCooldown = 2;
   }

   public void tick() {
      if (this.resetXRotOnTick()) {
         this.mob.setXRot(0.0F);
      }

      if (this.lookAtCooldown > 0) {
         this.lookAtCooldown--;
         this.getYRotD().ifPresent($$0 -> this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, $$0, this.yMaxRotSpeed));
         this.getXRotD().ifPresent($$0 -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), $$0, this.xMaxRotAngle)));
      } else {
         this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
      }

      this.clampHeadRotationToBody();
   }

   protected void clampHeadRotationToBody() {
      if (!this.mob.getNavigation().isDone()) {
         this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, this.mob.getMaxHeadYRot());
      }
   }

   protected boolean resetXRotOnTick() {
      return true;
   }

   public boolean isLookingAtTarget() {
      return this.lookAtCooldown > 0;
   }

   public double getWantedX() {
      return this.wantedX;
   }

   public double getWantedY() {
      return this.wantedY;
   }

   public double getWantedZ() {
      return this.wantedZ;
   }

   protected Optional<Float> getXRotD() {
      double $$0 = this.wantedX - this.mob.getX();
      double $$1 = this.wantedY - this.mob.getEyeY();
      double $$2 = this.wantedZ - this.mob.getZ();
      double $$3 = Math.sqrt($$0 * $$0 + $$2 * $$2);
      return !(Math.abs($$1) > 1.0E-5F) && !(Math.abs($$3) > 1.0E-5F)
         ? Optional.empty()
         : Optional.of((float)(-(Mth.atan2($$1, $$3) * 180.0F / (float)Math.PI)));
   }

   protected Optional<Float> getYRotD() {
      double $$0 = this.wantedX - this.mob.getX();
      double $$1 = this.wantedZ - this.mob.getZ();
      return !(Math.abs($$1) > 1.0E-5F) && !(Math.abs($$0) > 1.0E-5F)
         ? Optional.empty()
         : Optional.of((float)(Mth.atan2($$1, $$0) * 180.0F / (float)Math.PI) - 90.0F);
   }
}
