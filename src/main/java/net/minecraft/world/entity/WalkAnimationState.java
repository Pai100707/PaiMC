package net.minecraft.world.entity;

import net.minecraft.util.Mth;

public class WalkAnimationState {
   private float speedOld;
   private float speed;
   private float position;
   private float positionScale = 1.0F;

   public void setSpeed(float $$0) {
      this.speed = $$0;
   }

   public void update(float $$0, float $$1, float $$2) {
      this.speedOld = this.speed;
      this.speed = this.speed + ($$0 - this.speed) * $$1;
      this.position = this.position + this.speed;
      this.positionScale = $$2;
   }

   public void stop() {
      this.speedOld = 0.0F;
      this.speed = 0.0F;
      this.position = 0.0F;
   }

   public float speed() {
      return this.speed;
   }

   public float speed(float $$0) {
      return Math.min(Mth.lerp($$0, this.speedOld, this.speed), 1.0F);
   }

   public float position() {
      return this.position * this.positionScale;
   }

   public float position(float $$0) {
      return (this.position - this.speed * (1.0F - $$0)) * this.positionScale;
   }

   public boolean isMoving() {
      return this.speed > 1.0E-5F;
   }
}
