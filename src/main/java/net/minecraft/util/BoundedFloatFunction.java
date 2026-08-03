package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.function.Function;

public interface BoundedFloatFunction<C> {
   net.minecraft.util.BoundedFloatFunction<Float> IDENTITY = createUnlimited($$0 -> $$0);

   float apply(C var1);

   float minValue();

   float maxValue();

   static net.minecraft.util.BoundedFloatFunction<Float> createUnlimited(final Float2FloatFunction $$0) {
      return new net.minecraft.util.BoundedFloatFunction<Float>() {
         public float apply(Float $$0x) {
            return (Float)$$0.apply($$0);
         }

         @Override
         public float minValue() {
            return Float.NEGATIVE_INFINITY;
         }

         @Override
         public float maxValue() {
            return Float.POSITIVE_INFINITY;
         }
      };
   }

   default <C2> net.minecraft.util.BoundedFloatFunction<C2> comap(final Function<C2, C> $$0) {
      final net.minecraft.util.BoundedFloatFunction<C> $$1 = this;
      return new net.minecraft.util.BoundedFloatFunction<C2>() {
         @Override
         public float apply(C2 $$0x) {
            return $$1.apply($$0.apply($$0));
         }

         @Override
         public float minValue() {
            return $$1.minValue();
         }

         @Override
         public float maxValue() {
            return $$1.maxValue();
         }
      };
   }
}
