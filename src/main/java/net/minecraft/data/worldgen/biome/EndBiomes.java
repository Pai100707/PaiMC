package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.placement.EndPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.BiomeGenerationSettings.Builder;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class EndBiomes {
   private static Biome baseEndBiome(Builder $$0) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$1 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.endSpawns($$1);
      return new BiomeBuilder()
         .hasPrecipitation(false)
         .temperature(0.5F)
         .downfall(0.5F)
         .specialEffects(new net.minecraft.world.level.biome.BiomeSpecialEffects.Builder().waterColor(4159204).build())
         .mobSpawnSettings($$1.build())
         .generationSettings($$0.build())
         .build();
   }

   public static Biome endBarrens(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      Builder $$2 = new Builder($$0, $$1);
      return baseEndBiome($$2);
   }

   public static Biome theEnd(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      Builder $$2 = new Builder($$0, $$1)
         .addFeature(Decoration.SURFACE_STRUCTURES, EndPlacements.END_SPIKE)
         .addFeature(Decoration.TOP_LAYER_MODIFICATION, EndPlacements.END_PLATFORM);
      return baseEndBiome($$2);
   }

   public static Biome endMidlands(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      Builder $$2 = new Builder($$0, $$1);
      return baseEndBiome($$2);
   }

   public static Biome endHighlands(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      Builder $$2 = new Builder($$0, $$1)
         .addFeature(Decoration.SURFACE_STRUCTURES, EndPlacements.END_GATEWAY_RETURN)
         .addFeature(Decoration.VEGETAL_DECORATION, EndPlacements.CHORUS_PLANT);
      return baseEndBiome($$2);
   }

   public static Biome smallEndIslands(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      Builder $$2 = new Builder($$0, $$1).addFeature(Decoration.RAW_GENERATION, EndPlacements.END_ISLAND_DECORATED);
      return baseEndBiome($$2);
   }
}
