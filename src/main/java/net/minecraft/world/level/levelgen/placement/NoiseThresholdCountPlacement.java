package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

public class NoiseThresholdCountPlacement extends RepeatingPlacement {
   public static final MapCodec<NoiseThresholdCountPlacement> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.DOUBLE.fieldOf("noise_level").forGetter($$0x -> $$0x.noiseLevel),
            Codec.INT.fieldOf("below_noise").forGetter($$0x -> $$0x.belowNoise),
            Codec.INT.fieldOf("above_noise").forGetter($$0x -> $$0x.aboveNoise)
         )
         .apply($$0, NoiseThresholdCountPlacement::new)
   );
   private final double noiseLevel;
   private final int belowNoise;
   private final int aboveNoise;

   private NoiseThresholdCountPlacement(double $$0, int $$1, int $$2) {
      this.noiseLevel = $$0;
      this.belowNoise = $$1;
      this.aboveNoise = $$2;
   }

   public static NoiseThresholdCountPlacement of(double $$0, int $$1, int $$2) {
      return new NoiseThresholdCountPlacement($$0, $$1, $$2);
   }

   @Override
   protected int count(RandomSource $$0, BlockPos $$1) {
      double $$2 = Biome.BIOME_INFO_NOISE.getValue($$1.getX() / 200.0, $$1.getZ() / 200.0, false);
      return $$2 < this.noiseLevel ? this.belowNoise : this.aboveNoise;
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.NOISE_THRESHOLD_COUNT;
   }
}
