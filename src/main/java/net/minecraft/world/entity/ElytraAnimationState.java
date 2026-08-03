package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ElytraAnimationState {
   private static final float DEFAULT_X_ROT = (float) (Math.PI / 12);
   private static final float DEFAULT_Z_ROT = (float) (-Math.PI / 12);
   private float rotX;
   private float rotY;
   private float rotZ;
   private float rotXOld;
   private float rotYOld;
   private float rotZOld;
   private final net.minecraft.world.entity.LivingEntity entity;

   public ElytraAnimationState(net.minecraft.world.entity.LivingEntity $$0) {
      this.entity = $$0;
   }

   public void tick() {
      this.rotXOld = this.rotX;
      this.rotYOld = this.rotY;
      this.rotZOld = this.rotZ;
      float $$3;
      float $$4;
      float $$5;
      if (this.entity.isFallFlying()) {
         float $$0 = 1.0F;
         Vec3 $$1 = this.entity.getDeltaMovement();
         if ($$1.y < 0.0) {
            Vec3 $$2 = $$1.normalize();
            $$0 = 1.0F - (float)Math.pow(-$$2.y, 1.5);
         }

         $$3 = Mth.lerp($$0, (float) (Math.PI / 12), (float) (Math.PI / 9));
         $$4 = Mth.lerp($$0, (float) (-Math.PI / 12), (float) (-Math.PI / 2));
         $$5 = 0.0F;
      } else if (this.entity.isCrouching()) {
         $$3 = (float) (Math.PI * 2.0 / 9.0);
         $$4 = (float) (-Math.PI / 4);
         $$5 = 0.08726646F;
      } else {
         $$3 = (float) (Math.PI / 12);
         $$4 = (float) (-Math.PI / 12);
         $$5 = 0.0F;
      }

      this.rotX = this.rotX + ($$3 - this.rotX) * 0.3F;
      this.rotY = this.rotY + ($$5 - this.rotY) * 0.3F;
      this.rotZ = this.rotZ + ($$4 - this.rotZ) * 0.3F;
   }

   public float getRotX(float $$0) {
      return Mth.lerp($$0, this.rotXOld, this.rotX);
   }

   public float getRotY(float $$0) {
      return Mth.lerp($$0, this.rotYOld, this.rotY);
   }

   public float getRotZ(float $$0) {
      return Mth.lerp($$0, this.rotZOld, this.rotZ);
   }
}
