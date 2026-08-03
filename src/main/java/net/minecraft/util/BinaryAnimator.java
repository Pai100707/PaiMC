package net.minecraft.util;

public class BinaryAnimator {
   private final int animationLength;
   private final net.minecraft.util.EasingType easing;
   private int ticks;
   private int ticksOld;

   public BinaryAnimator(int $$0, net.minecraft.util.EasingType $$1) {
      this.animationLength = $$0;
      this.easing = $$1;
   }

   public BinaryAnimator(int $$0) {
      this($$0, net.minecraft.util.EasingType.LINEAR);
   }

   public void tick(boolean $$0) {
      this.ticksOld = this.ticks;
      if ($$0) {
         if (this.ticks < this.animationLength) {
            this.ticks++;
         }
      } else if (this.ticks > 0) {
         this.ticks--;
      }
   }

   public float getFactor(float $$0) {
      float $$1 = net.minecraft.util.Mth.lerp($$0, (float)this.ticksOld, (float)this.ticks) / this.animationLength;
      return this.easing.apply($$1);
   }
}
