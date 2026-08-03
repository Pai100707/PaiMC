package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

public class BiomeFilter extends PlacementFilter {
   private static final BiomeFilter INSTANCE = new BiomeFilter();
   public static MapCodec<BiomeFilter> CODEC = MapCodec.unit(() -> INSTANCE);

   private BiomeFilter() {
   }

   public static BiomeFilter biome() {
      return INSTANCE;
   }

   @Override
   protected boolean shouldPlace(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      PlacedFeature $$3 = $$0.topFeature()
         .orElseThrow(() -> new IllegalStateException("Tried to biome check an unregistered feature, or a feature that should not restrict the biome"));
      Holder<Biome> $$4 = $$0.getLevel().getBiome($$2);
      return $$0.generator().getBiomeGenerationSettings($$4).hasFeature($$3);
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.BIOME_FILTER;
   }
}
