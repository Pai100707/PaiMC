package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class ConstantFloat extends FloatProvider {
   public static final ConstantFloat ZERO = new ConstantFloat(0.0F);
   public static final MapCodec<ConstantFloat> CODEC = Codec.FLOAT.fieldOf("value").xmap(ConstantFloat::of, ConstantFloat::getValue);
   private final float value;

   public static ConstantFloat of(float $$0) {
      return $$0 == 0.0F ? ZERO : new ConstantFloat($$0);
   }

   private ConstantFloat(float $$0) {
      this.value = $$0;
   }

   public float getValue() {
      return this.value;
   }

   @Override
   public float sample(net.minecraft.util.RandomSource $$0) {
      return this.value;
   }

   @Override
   public float getMinValue() {
      return this.value;
   }

   @Override
   public float getMaxValue() {
      return this.value;
   }

   @Override
   public FloatProviderType<?> getType() {
      return FloatProviderType.CONSTANT;
   }

   @Override
   public String toString() {
      return Float.toString(this.value);
   }
}
