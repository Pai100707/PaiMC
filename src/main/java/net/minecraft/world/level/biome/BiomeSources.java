package net.minecraft.world.level.biome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;

public class BiomeSources {
   public static MapCodec<? extends BiomeSource> bootstrap(Registry<MapCodec<? extends BiomeSource>> $$0) {
      Registry.register($$0, "fixed", FixedBiomeSource.CODEC);
      Registry.register($$0, "multi_noise", MultiNoiseBiomeSource.CODEC);
      Registry.register($$0, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
      return (MapCodec<? extends BiomeSource>)Registry.register($$0, "the_end", TheEndBiomeSource.CODEC);
   }
}
