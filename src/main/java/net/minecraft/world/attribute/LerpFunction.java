package net.minecraft.world.attribute;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public interface LerpFunction<T> {
   static net.minecraft.world.attribute.LerpFunction<Float> ofFloat() {
      return Mth::lerp;
   }

   static net.minecraft.world.attribute.LerpFunction<Float> ofDegrees(float $$0) {
      return ($$1, $$2, $$3) -> {
         float $$4 = Mth.wrapDegrees($$3 - $$2);
         return Math.abs($$4) >= $$0 ? $$3 : $$2 + $$1 * $$4;
      };
   }

   static <T> net.minecraft.world.attribute.LerpFunction<T> ofConstant() {
      return ($$0, $$1, $$2) -> $$1;
   }

   static <T> net.minecraft.world.attribute.LerpFunction<T> ofStep(float $$0) {
      return ($$1, $$2, $$3) -> $$1 >= $$0 ? $$3 : $$2;
   }

   static net.minecraft.world.attribute.LerpFunction<Integer> ofColor() {
      return ARGB::srgbLerp;
   }

   T apply(float var1, T var2, T var3);
}
