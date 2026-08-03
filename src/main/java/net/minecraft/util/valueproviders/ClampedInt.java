package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ClampedInt extends IntProvider {
   public static final MapCodec<ClampedInt> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               IntProvider.CODEC.fieldOf("source").forGetter($$0x -> $$0x.source),
               Codec.INT.fieldOf("min_inclusive").forGetter($$0x -> $$0x.minInclusive),
               Codec.INT.fieldOf("max_inclusive").forGetter($$0x -> $$0x.maxInclusive)
            )
            .apply($$0, ClampedInt::new)
      )
      .validate(
         $$0 -> $$0.maxInclusive < $$0.minInclusive
            ? DataResult.error(() -> "Max must be at least min, min_inclusive: " + $$0.minInclusive + ", max_inclusive: " + $$0.maxInclusive)
            : DataResult.success($$0)
      );
   private final IntProvider source;
   private final int minInclusive;
   private final int maxInclusive;

   public static ClampedInt of(IntProvider $$0, int $$1, int $$2) {
      return new ClampedInt($$0, $$1, $$2);
   }

   public ClampedInt(IntProvider $$0, int $$1, int $$2) {
      this.source = $$0;
      this.minInclusive = $$1;
      this.maxInclusive = $$2;
   }

   @Override
   public int sample(net.minecraft.util.RandomSource $$0) {
      return net.minecraft.util.Mth.clamp(this.source.sample($$0), this.minInclusive, this.maxInclusive);
   }

   @Override
   public int getMinValue() {
      return Math.max(this.minInclusive, this.source.getMinValue());
   }

   @Override
   public int getMaxValue() {
      return Math.min(this.maxInclusive, this.source.getMaxValue());
   }

   @Override
   public IntProviderType<?> getType() {
      return IntProviderType.CLAMPED;
   }
}
