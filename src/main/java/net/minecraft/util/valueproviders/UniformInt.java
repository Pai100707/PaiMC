package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class UniformInt extends IntProvider {
   public static final MapCodec<UniformInt> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.INT.fieldOf("min_inclusive").forGetter($$0x -> $$0x.minInclusive), Codec.INT.fieldOf("max_inclusive").forGetter($$0x -> $$0x.maxInclusive)
            )
            .apply($$0, UniformInt::new)
      )
      .validate(
         $$0 -> $$0.maxInclusive < $$0.minInclusive
            ? DataResult.error(() -> "Max must be at least min, min_inclusive: " + $$0.minInclusive + ", max_inclusive: " + $$0.maxInclusive)
            : DataResult.success($$0)
      );
   private final int minInclusive;
   private final int maxInclusive;

   private UniformInt(int $$0, int $$1) {
      this.minInclusive = $$0;
      this.maxInclusive = $$1;
   }

   public static UniformInt of(int $$0, int $$1) {
      return new UniformInt($$0, $$1);
   }

   @Override
   public int sample(net.minecraft.util.RandomSource $$0) {
      return net.minecraft.util.Mth.randomBetweenInclusive($$0, this.minInclusive, this.maxInclusive);
   }

   @Override
   public int getMinValue() {
      return this.minInclusive;
   }

   @Override
   public int getMaxValue() {
      return this.maxInclusive;
   }

   @Override
   public IntProviderType<?> getType() {
      return IntProviderType.UNIFORM;
   }

   @Override
   public String toString() {
      return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
   }
}
