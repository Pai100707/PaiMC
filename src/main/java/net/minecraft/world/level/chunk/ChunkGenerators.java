package net.minecraft.world.level.chunk;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

public class ChunkGenerators {
   public static MapCodec<? extends ChunkGenerator> bootstrap(Registry<MapCodec<? extends ChunkGenerator>> $$0) {
      Registry.register($$0, "noise", NoiseBasedChunkGenerator.CODEC);
      Registry.register($$0, "flat", FlatLevelSource.CODEC);
      return (MapCodec<? extends ChunkGenerator>)Registry.register($$0, "debug", DebugLevelSource.CODEC);
   }
}
