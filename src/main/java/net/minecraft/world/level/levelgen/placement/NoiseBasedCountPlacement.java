package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

public class NoiseBasedCountPlacement extends RepeatingPlacement {
   public static final MapCodec<NoiseBasedCountPlacement> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.INT.fieldOf("noise_to_count_ratio").forGetter($$0x -> $$0x.noiseToCountRatio),
            Codec.DOUBLE.fieldOf("noise_factor").forGetter($$0x -> $$0x.noiseFactor),
            Codec.DOUBLE.fieldOf("noise_offset").orElse(0.0).forGetter($$0x -> $$0x.noiseOffset)
         )
         .apply($$0, NoiseBasedCountPlacement::new)
   );
   private final int noiseToCountRatio;
   private final double noiseFactor;
   private final double noiseOffset;

   private NoiseBasedCountPlacement(int $$0, double $$1, double $$2) {
      this.noiseToCountRatio = $$0;
      this.noiseFactor = $$1;
      this.noiseOffset = $$2;
   }

   public static NoiseBasedCountPlacement of(int $$0, double $$1, double $$2) {
      return new NoiseBasedCountPlacement($$0, $$1, $$2);
   }

   @Override
   protected int count(RandomSource $$0, BlockPos $$1) {
      double $$2 = Biome.BIOME_INFO_NOISE.getValue($$1.getX() / this.noiseFactor, $$1.getZ() / this.noiseFactor, false);
      return (int)Math.ceil(($$2 + this.noiseOffset) * this.noiseToCountRatio);
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.NOISE_BASED_COUNT;
   }
}
