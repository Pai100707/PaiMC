package net.minecraft.world.level.levelgen.carver;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext extends WorldGenerationContext {
   private final RegistryAccess registryAccess;
   private final NoiseChunk noiseChunk;
   private final RandomState randomState;
   private final SurfaceRules.RuleSource surfaceRule;

   public CarvingContext(
      NoiseBasedChunkGenerator $$0,
      RegistryAccess $$1,
      net.minecraft.world.level.LevelHeightAccessor $$2,
      NoiseChunk $$3,
      RandomState $$4,
      SurfaceRules.RuleSource $$5
   ) {
      super($$0, $$2);
      this.registryAccess = $$1;
      this.noiseChunk = $$3;
      this.randomState = $$4;
      this.surfaceRule = $$5;
   }

   @Deprecated
   public Optional<BlockState> topMaterial(Function<BlockPos, Holder<Biome>> $$0, ChunkAccess $$1, BlockPos $$2, boolean $$3) {
      return this.randomState.surfaceSystem().topMaterial(this.surfaceRule, this, $$0, $$1, this.noiseChunk, $$2, $$3);
   }

   @Deprecated
   public RegistryAccess registryAccess() {
      return this.registryAccess;
   }

   public RandomState randomState() {
      return this.randomState;
   }
}
