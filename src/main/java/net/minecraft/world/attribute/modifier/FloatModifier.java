package net.minecraft.world.attribute.modifier;

import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;

public interface FloatModifier<Argument> extends AttributeModifier<Float, Argument> {
   FloatModifier<FloatWithAlpha> ALPHA_BLEND = new FloatModifier<FloatWithAlpha>() {
      public Float apply(Float $$0, FloatWithAlpha $$1) {
         return Mth.lerp($$1.alpha(), $$0, $$1.value());
      }

      @Override
      public Codec<FloatWithAlpha> argumentCodec(net.minecraft.world.attribute.EnvironmentAttribute<Float> $$0) {
         return FloatWithAlpha.CODEC;
      }

      @Override
      public net.minecraft.world.attribute.LerpFunction<FloatWithAlpha> argumentKeyframeLerp(net.minecraft.world.attribute.EnvironmentAttribute<Float> $$0) {
         return ($$0x, $$1, $$2) -> new FloatWithAlpha(Mth.lerp($$0x, $$1.value(), $$2.value()), Mth.lerp($$0x, $$1.alpha(), $$2.alpha()));
      }
   };
   FloatModifier<Float> ADD = Float::sum;
   FloatModifier<Float> SUBTRACT = (FloatModifier.Simple)($$0, $$1) -> $$0 - $$1;
   FloatModifier<Float> MULTIPLY = (FloatModifier.Simple)($$0, $$1) -> $$0 * $$1;
   FloatModifier<Float> MINIMUM = Math::min;
   FloatModifier<Float> MAXIMUM = Math::max;

   @FunctionalInterface
   public interface Simple extends FloatModifier<Float> {
      @Override
      default Codec<Float> argumentCodec(net.minecraft.world.attribute.EnvironmentAttribute<Float> $$0) {
         return Codec.FLOAT;
      }

      @Override
      default net.minecraft.world.attribute.LerpFunction<Float> argumentKeyframeLerp(net.minecraft.world.attribute.EnvironmentAttribute<Float> $$0) {
         return net.minecraft.world.attribute.LerpFunction.ofFloat();
      }
   }
}
