/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.placement.EndPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class EndBiomes {
    private static Biome baseEndBiome(BiomeGenerationSettings.Builder $$0) {
        MobSpawnSettings.Builder $$1 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.endSpawns($$1);
        return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.5f).downfall(0.5f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).build()).mobSpawnSettings($$1.build()).generationSettings($$0.build()).build();
    }

    public static Biome endBarrens(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
        BiomeGenerationSettings.Builder $$2 = new BiomeGenerationSettings.Builder($$0, $$1);
        return EndBiomes.baseEndBiome($$2);
    }

    public static Biome theEnd(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
        BiomeGenerationSettings.Builder $$2 = new BiomeGenerationSettings.Builder($$0, $$1).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, EndPlacements.END_SPIKE).addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, EndPlacements.END_PLATFORM);
        return EndBiomes.baseEndBiome($$2);
    }

    public static Biome endMidlands(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
        BiomeGenerationSettings.Builder $$2 = new BiomeGenerationSettings.Builder($$0, $$1);
        return EndBiomes.baseEndBiome($$2);
    }

    public static Biome endHighlands(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
        BiomeGenerationSettings.Builder $$2 = new BiomeGenerationSettings.Builder($$0, $$1).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, EndPlacements.END_GATEWAY_RETURN).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, EndPlacements.CHORUS_PLANT);
        return EndBiomes.baseEndBiome($$2);
    }

    public static Biome smallEndIslands(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
        BiomeGenerationSettings.Builder $$2 = new BiomeGenerationSettings.Builder($$0, $$1).addFeature(GenerationStep.Decoration.RAW_GENERATION, EndPlacements.END_ISLAND_DECORATED);
        return EndBiomes.baseEndBiome($$2);
    }
}

