package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class UniformFloat extends FloatProvider {
   public static final MapCodec<UniformFloat> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.FLOAT.fieldOf("min_inclusive").forGetter($$0x -> $$0x.minInclusive),
               Codec.FLOAT.fieldOf("max_exclusive").forGetter($$0x -> $$0x.maxExclusive)
            )
            .apply($$0, UniformFloat::new)
      )
      .validate(
         $$0 -> $$0.maxExclusive <= $$0.minInclusive
            ? DataResult.error(() -> "Max must be larger than min, min_inclusive: " + $$0.minInclusive + ", max_exclusive: " + $$0.maxExclusive)
            : DataResult.success($$0)
      );
   private final float minInclusive;
   private final float maxExclusive;

   private UniformFloat(float $$0, float $$1) {
      this.minInclusive = $$0;
      this.maxExclusive = $$1;
   }

   public static UniformFloat of(float $$0, float $$1) {
      if ($$1 <= $$0) {
         throw new IllegalArgumentException("Max must exceed min");
      } else {
         return new UniformFloat($$0, $$1);
      }
   }

   @Override
   public float sample(net.minecraft.util.RandomSource $$0) {
      return net.minecraft.util.Mth.randomBetween($$0, this.minInclusive, this.maxExclusive);
   }

   @Override
   public float getMinValue() {
      return this.minInclusive;
   }

   @Override
   public float getMaxValue() {
      return this.maxExclusive;
   }

   @Override
   public FloatProviderType<?> getType() {
      return FloatProviderType.UNIFORM;
   }

   @Override
   public String toString() {
      return "[" + this.minInclusive + "-" + this.maxExclusive + "]";
   }
}
