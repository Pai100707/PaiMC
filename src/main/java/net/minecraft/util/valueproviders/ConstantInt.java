package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class ConstantInt extends IntProvider {
   public static final ConstantInt ZERO = new ConstantInt(0);
   public static final MapCodec<ConstantInt> CODEC = Codec.INT.fieldOf("value").xmap(ConstantInt::of, ConstantInt::getValue);
   private final int value;

   public static ConstantInt of(int $$0) {
      return $$0 == 0 ? ZERO : new ConstantInt($$0);
   }

   private ConstantInt(int $$0) {
      this.value = $$0;
   }

   public int getValue() {
      return this.value;
   }

   @Override
   public int sample(net.minecraft.util.RandomSource $$0) {
      return this.value;
   }

   @Override
   public int getMinValue() {
      return this.value;
   }

   @Override
   public int getMaxValue() {
      return this.value;
   }

   @Override
   public IntProviderType<?> getType() {
      return IntProviderType.CONSTANT;
   }

   @Override
   public String toString() {
      return Integer.toString(this.value);
   }
}
