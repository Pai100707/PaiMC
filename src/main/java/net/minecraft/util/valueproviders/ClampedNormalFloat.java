package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ClampedNormalFloat extends FloatProvider {
   public static final MapCodec<ClampedNormalFloat> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.FLOAT.fieldOf("mean").forGetter($$0x -> $$0x.mean),
               Codec.FLOAT.fieldOf("deviation").forGetter($$0x -> $$0x.deviation),
               Codec.FLOAT.fieldOf("min").forGetter($$0x -> $$0x.min),
               Codec.FLOAT.fieldOf("max").forGetter($$0x -> $$0x.max)
            )
            .apply($$0, ClampedNormalFloat::new)
      )
      .validate($$0 -> $$0.max < $$0.min ? DataResult.error(() -> "Max must be larger than min: [" + $$0.min + ", " + $$0.max + "]") : DataResult.success($$0));
   private final float mean;
   private final float deviation;
   private final float min;
   private final float max;

   public static ClampedNormalFloat of(float $$0, float $$1, float $$2, float $$3) {
      return new ClampedNormalFloat($$0, $$1, $$2, $$3);
   }

   private ClampedNormalFloat(float $$0, float $$1, float $$2, float $$3) {
      this.mean = $$0;
      this.deviation = $$1;
      this.min = $$2;
      this.max = $$3;
   }

   @Override
   public float sample(net.minecraft.util.RandomSource $$0) {
      return sample($$0, this.mean, this.deviation, this.min, this.max);
   }

   public static float sample(net.minecraft.util.RandomSource $$0, float $$1, float $$2, float $$3, float $$4) {
      return net.minecraft.util.Mth.clamp(net.minecraft.util.Mth.normal($$0, $$1, $$2), $$3, $$4);
   }

   @Override
   public float getMinValue() {
      return this.min;
   }

   @Override
   public float getMaxValue() {
      return this.max;
   }

   @Override
   public FloatProviderType<?> getType() {
      return FloatProviderType.CLAMPED_NORMAL;
   }

   @Override
   public String toString() {
      return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min + "-" + this.max + "]";
   }
}
