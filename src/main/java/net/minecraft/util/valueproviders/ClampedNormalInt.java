package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ClampedNormalInt extends IntProvider {
   public static final MapCodec<ClampedNormalInt> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.FLOAT.fieldOf("mean").forGetter($$0x -> $$0x.mean),
               Codec.FLOAT.fieldOf("deviation").forGetter($$0x -> $$0x.deviation),
               Codec.INT.fieldOf("min_inclusive").forGetter($$0x -> $$0x.minInclusive),
               Codec.INT.fieldOf("max_inclusive").forGetter($$0x -> $$0x.maxInclusive)
            )
            .apply($$0, ClampedNormalInt::new)
      )
      .validate(
         $$0 -> $$0.maxInclusive < $$0.minInclusive
            ? DataResult.error(() -> "Max must be larger than min: [" + $$0.minInclusive + ", " + $$0.maxInclusive + "]")
            : DataResult.success($$0)
      );
   private final float mean;
   private final float deviation;
   private final int minInclusive;
   private final int maxInclusive;

   public static ClampedNormalInt of(float $$0, float $$1, int $$2, int $$3) {
      return new ClampedNormalInt($$0, $$1, $$2, $$3);
   }

   private ClampedNormalInt(float $$0, float $$1, int $$2, int $$3) {
      this.mean = $$0;
      this.deviation = $$1;
      this.minInclusive = $$2;
      this.maxInclusive = $$3;
   }

   @Override
   public int sample(net.minecraft.util.RandomSource $$0) {
      return sample($$0, this.mean, this.deviation, this.minInclusive, this.maxInclusive);
   }

   public static int sample(net.minecraft.util.RandomSource $$0, float $$1, float $$2, float $$3, float $$4) {
      return (int)net.minecraft.util.Mth.clamp(net.minecraft.util.Mth.normal($$0, $$1, $$2), $$3, $$4);
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
      return IntProviderType.CLAMPED_NORMAL;
   }

   @Override
   public String toString() {
      return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
   }
}
