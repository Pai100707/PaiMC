package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.stream.IntStream;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class BlendedNoise implements DensityFunction.SimpleFunction {
   private static final Codec<Double> SCALE_RANGE = Codec.doubleRange(0.001, 1000.0);
   private static final MapCodec<BlendedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            SCALE_RANGE.fieldOf("xz_scale").forGetter($$0x -> $$0x.xzScale),
            SCALE_RANGE.fieldOf("y_scale").forGetter($$0x -> $$0x.yScale),
            SCALE_RANGE.fieldOf("xz_factor").forGetter($$0x -> $$0x.xzFactor),
            SCALE_RANGE.fieldOf("y_factor").forGetter($$0x -> $$0x.yFactor),
            Codec.doubleRange(1.0, 8.0).fieldOf("smear_scale_multiplier").forGetter($$0x -> $$0x.smearScaleMultiplier)
         )
         .apply($$0, BlendedNoise::createUnseeded)
   );
   public static final KeyDispatchDataCodec<BlendedNoise> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);
   private final PerlinNoise minLimitNoise;
   private final PerlinNoise maxLimitNoise;
   private final PerlinNoise mainNoise;
   private final double xzMultiplier;
   private final double yMultiplier;
   private final double xzFactor;
   private final double yFactor;
   private final double smearScaleMultiplier;
   private final double maxValue;
   private final double xzScale;
   private final double yScale;

   public static BlendedNoise createUnseeded(double $$0, double $$1, double $$2, double $$3, double $$4) {
      return new BlendedNoise(new XoroshiroRandomSource(0L), $$0, $$1, $$2, $$3, $$4);
   }

   private BlendedNoise(PerlinNoise $$0, PerlinNoise $$1, PerlinNoise $$2, double $$3, double $$4, double $$5, double $$6, double $$7) {
      this.minLimitNoise = $$0;
      this.maxLimitNoise = $$1;
      this.mainNoise = $$2;
      this.xzScale = $$3;
      this.yScale = $$4;
      this.xzFactor = $$5;
      this.yFactor = $$6;
      this.smearScaleMultiplier = $$7;
      this.xzMultiplier = 684.412 * this.xzScale;
      this.yMultiplier = 684.412 * this.yScale;
      this.maxValue = $$0.maxBrokenValue(this.yMultiplier);
   }

   @VisibleForTesting
   public BlendedNoise(RandomSource $$0, double $$1, double $$2, double $$3, double $$4, double $$5) {
      this(
         PerlinNoise.createLegacyForBlendedNoise($$0, IntStream.rangeClosed(-15, 0)),
         PerlinNoise.createLegacyForBlendedNoise($$0, IntStream.rangeClosed(-15, 0)),
         PerlinNoise.createLegacyForBlendedNoise($$0, IntStream.rangeClosed(-7, 0)),
         $$1,
         $$2,
         $$3,
         $$4,
         $$5
      );
   }

   public BlendedNoise withNewRandom(RandomSource $$0) {
      return new BlendedNoise($$0, this.xzScale, this.yScale, this.xzFactor, this.yFactor, this.smearScaleMultiplier);
   }

   @Override
   public double compute(DensityFunction.FunctionContext $$0) {
      double $$1 = $$0.blockX() * this.xzMultiplier;
      double $$2 = $$0.blockY() * this.yMultiplier;
      double $$3 = $$0.blockZ() * this.xzMultiplier;
      double $$4 = $$1 / this.xzFactor;
      double $$5 = $$2 / this.yFactor;
      double $$6 = $$3 / this.xzFactor;
      double $$7 = this.yMultiplier * this.smearScaleMultiplier;
      double $$8 = $$7 / this.yFactor;
      double $$9 = 0.0;
      double $$10 = 0.0;
      double $$11 = 0.0;
      boolean $$12 = true;
      double $$13 = 1.0;

      for (int $$14 = 0; $$14 < 8; $$14++) {
         ImprovedNoise $$15 = this.mainNoise.getOctaveNoise($$14);
         if ($$15 != null) {
            $$11 += $$15.noise(PerlinNoise.wrap($$4 * $$13), PerlinNoise.wrap($$5 * $$13), PerlinNoise.wrap($$6 * $$13), $$8 * $$13, $$5 * $$13) / $$13;
         }

         $$13 /= 2.0;
      }

      double $$16 = ($$11 / 10.0 + 1.0) / 2.0;
      boolean $$17 = $$16 >= 1.0;
      boolean $$18 = $$16 <= 0.0;
      $$13 = 1.0;

      for (int $$19 = 0; $$19 < 16; $$19++) {
         double $$20 = PerlinNoise.wrap($$1 * $$13);
         double $$21 = PerlinNoise.wrap($$2 * $$13);
         double $$22 = PerlinNoise.wrap($$3 * $$13);
         double $$23 = $$7 * $$13;
         if (!$$17) {
            ImprovedNoise $$24 = this.minLimitNoise.getOctaveNoise($$19);
            if ($$24 != null) {
               $$9 += $$24.noise($$20, $$21, $$22, $$23, $$2 * $$13) / $$13;
            }
         }

         if (!$$18) {
            ImprovedNoise $$25 = this.maxLimitNoise.getOctaveNoise($$19);
            if ($$25 != null) {
               $$10 += $$25.noise($$20, $$21, $$22, $$23, $$2 * $$13) / $$13;
            }
         }

         $$13 /= 2.0;
      }

      return Mth.clampedLerp($$16, $$9 / 512.0, $$10 / 512.0) / 128.0;
   }

   @Override
   public double minValue() {
      return -this.maxValue();
   }

   @Override
   public double maxValue() {
      return this.maxValue;
   }

   @VisibleForTesting
   public void parityConfigString(StringBuilder $$0) {
      $$0.append("BlendedNoise{minLimitNoise=");
      this.minLimitNoise.parityConfigString($$0);
      $$0.append(", maxLimitNoise=");
      this.maxLimitNoise.parityConfigString($$0);
      $$0.append(", mainNoise=");
      this.mainNoise.parityConfigString($$0);
      $$0.append(
            String.format(
               Locale.ROOT,
               ", xzScale=%.3f, yScale=%.3f, xzMainScale=%.3f, yMainScale=%.3f, cellWidth=4, cellHeight=8",
               684.412,
               684.412,
               8.555150000000001,
               4.277575000000001
            )
         )
         .append('}');
   }

   @Override
   public KeyDispatchDataCodec<? extends DensityFunction> codec() {
      return CODEC;
   }
}
