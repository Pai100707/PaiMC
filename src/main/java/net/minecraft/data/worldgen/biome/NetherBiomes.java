package net.minecraft.data.worldgen.biome;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.NetherPlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.attribute.AmbientAdditionsSettings;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.BiomeSpecialEffects.Builder;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class NetherBiomes {
   private static BiomeBuilder baseBiome() {
      return new BiomeBuilder().hasPrecipitation(false).temperature(2.0F).downfall(0.0F).specialEffects(new Builder().waterColor(4159204).build());
   }

   public static Biome netherWastes(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      MobSpawnSettings $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder()
         .addSpawn(MobCategory.MONSTER, 50, new SpawnerData(EntityType.GHAST, 4, 4))
         .addSpawn(MobCategory.MONSTER, 100, new SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 4, 4))
         .addSpawn(MobCategory.MONSTER, 2, new SpawnerData(EntityType.MAGMA_CUBE, 4, 4))
         .addSpawn(MobCategory.MONSTER, 1, new SpawnerData(EntityType.ENDERMAN, 4, 4))
         .addSpawn(MobCategory.MONSTER, 15, new SpawnerData(EntityType.PIGLIN, 4, 4))
         .addSpawn(MobCategory.CREATURE, 60, new SpawnerData(EntityType.STRIDER, 1, 2))
         .build();
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1)
         .addCarver(Carvers.NETHER_CAVE)
         .addFeature(Decoration.VEGETAL_DECORATION, MiscOverworldPlacements.SPRING_LAVA);
      BiomeDefaultFeatures.addDefaultMushrooms($$3);
      $$3.addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_FIRE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_SOUL_FIRE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, VegetationPlacements.BROWN_MUSHROOM_NETHER)
         .addFeature(Decoration.UNDERGROUND_DECORATION, VegetationPlacements.RED_MUSHROOM_NETHER)
         .addFeature(Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED);
      BiomeDefaultFeatures.addNetherDefaultOres($$3);
      return baseBiome()
         .setAttribute(EnvironmentAttributes.FOG_COLOR, -13432824)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES))
         .setAttribute(
            EnvironmentAttributes.AMBIENT_SOUNDS,
            new AmbientSounds(
               Optional.of(SoundEvents.AMBIENT_NETHER_WASTES_LOOP),
               Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0)),
               List.of(new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111))
            )
         )
         .mobSpawnSettings($$2)
         .generationSettings($$3.build())
         .build();
   }

   public static Biome soulSandValley(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      double $$2 = 0.7;
      double $$3 = 0.15;
      MobSpawnSettings $$4 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder()
         .addSpawn(MobCategory.MONSTER, 20, new SpawnerData(EntityType.SKELETON, 5, 5))
         .addSpawn(MobCategory.MONSTER, 50, new SpawnerData(EntityType.GHAST, 4, 4))
         .addSpawn(MobCategory.MONSTER, 1, new SpawnerData(EntityType.ENDERMAN, 4, 4))
         .addSpawn(MobCategory.CREATURE, 60, new SpawnerData(EntityType.STRIDER, 1, 2))
         .addMobCharge(EntityType.SKELETON, 0.7, 0.15)
         .addMobCharge(EntityType.GHAST, 0.7, 0.15)
         .addMobCharge(EntityType.ENDERMAN, 0.7, 0.15)
         .addMobCharge(EntityType.STRIDER, 0.7, 0.15)
         .build();
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$5 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1)
         .addCarver(Carvers.NETHER_CAVE)
         .addFeature(Decoration.VEGETAL_DECORATION, MiscOverworldPlacements.SPRING_LAVA)
         .addFeature(Decoration.LOCAL_MODIFICATIONS, NetherPlacements.BASALT_PILLAR)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_FIRE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_SOUL_FIRE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_CRIMSON_ROOTS)
         .addFeature(Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED)
         .addFeature(Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_SOUL_SAND);
      BiomeDefaultFeatures.addNetherDefaultOres($$5);
      return baseBiome()
         .setAttribute(EnvironmentAttributes.FOG_COLOR, -14989499)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY))
         .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(ParticleTypes.ASH, 0.00625F))
         .setAttribute(
            EnvironmentAttributes.AMBIENT_SOUNDS,
            new AmbientSounds(
               Optional.of(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP),
               Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0)),
               List.of(new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111))
            )
         )
         .mobSpawnSettings($$4)
         .generationSettings($$5.build())
         .build();
   }

   public static Biome basaltDeltas(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      MobSpawnSettings $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder()
         .addSpawn(MobCategory.MONSTER, 40, new SpawnerData(EntityType.GHAST, 1, 1))
         .addSpawn(MobCategory.MONSTER, 100, new SpawnerData(EntityType.MAGMA_CUBE, 2, 5))
         .addSpawn(MobCategory.CREATURE, 60, new SpawnerData(EntityType.STRIDER, 1, 2))
         .build();
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1)
         .addCarver(Carvers.NETHER_CAVE)
         .addFeature(Decoration.SURFACE_STRUCTURES, NetherPlacements.DELTA)
         .addFeature(Decoration.SURFACE_STRUCTURES, NetherPlacements.SMALL_BASALT_COLUMNS)
         .addFeature(Decoration.SURFACE_STRUCTURES, NetherPlacements.LARGE_BASALT_COLUMNS)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.BASALT_BLOBS)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.BLACKSTONE_BLOBS)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_DELTA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_FIRE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_SOUL_FIRE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, VegetationPlacements.BROWN_MUSHROOM_NETHER)
         .addFeature(Decoration.UNDERGROUND_DECORATION, VegetationPlacements.RED_MUSHROOM_NETHER)
         .addFeature(Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED_DOUBLE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_GOLD_DELTAS)
         .addFeature(Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_QUARTZ_DELTAS);
      BiomeDefaultFeatures.addAncientDebris($$3);
      return baseBiome()
         .setAttribute(EnvironmentAttributes.FOG_COLOR, -9937040)
         .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(ParticleTypes.WHITE_ASH, 0.118093334F))
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS))
         .setAttribute(
            EnvironmentAttributes.AMBIENT_SOUNDS,
            new AmbientSounds(
               Optional.of(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP),
               Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0)),
               List.of(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111))
            )
         )
         .mobSpawnSettings($$2)
         .generationSettings($$3.build())
         .build();
   }

   public static Biome crimsonForest(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      MobSpawnSettings $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder()
         .addSpawn(MobCategory.MONSTER, 1, new SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 2, 4))
         .addSpawn(MobCategory.MONSTER, 9, new SpawnerData(EntityType.HOGLIN, 3, 4))
         .addSpawn(MobCategory.MONSTER, 5, new SpawnerData(EntityType.PIGLIN, 3, 4))
         .addSpawn(MobCategory.CREATURE, 60, new SpawnerData(EntityType.STRIDER, 1, 2))
         .build();
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1)
         .addCarver(Carvers.NETHER_CAVE)
         .addFeature(Decoration.VEGETAL_DECORATION, MiscOverworldPlacements.SPRING_LAVA);
      BiomeDefaultFeatures.addDefaultMushrooms($$3);
      $$3.addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_FIRE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED)
         .addFeature(Decoration.VEGETAL_DECORATION, NetherPlacements.WEEPING_VINES)
         .addFeature(Decoration.VEGETAL_DECORATION, TreePlacements.CRIMSON_FUNGI)
         .addFeature(Decoration.VEGETAL_DECORATION, NetherPlacements.CRIMSON_FOREST_VEGETATION);
      BiomeDefaultFeatures.addNetherDefaultOres($$3);
      return baseBiome()
         .setAttribute(EnvironmentAttributes.FOG_COLOR, -13434109)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_CRIMSON_FOREST))
         .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(ParticleTypes.CRIMSON_SPORE, 0.025F))
         .setAttribute(
            EnvironmentAttributes.AMBIENT_SOUNDS,
            new AmbientSounds(
               Optional.of(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP),
               Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0)),
               List.of(new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111))
            )
         )
         .mobSpawnSettings($$2)
         .generationSettings($$3.build())
         .build();
   }

   public static Biome warpedForest(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      MobSpawnSettings $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder()
         .addSpawn(MobCategory.MONSTER, 1, new SpawnerData(EntityType.ENDERMAN, 4, 4))
         .addSpawn(MobCategory.CREATURE, 60, new SpawnerData(EntityType.STRIDER, 1, 2))
         .addMobCharge(EntityType.ENDERMAN, 1.0, 0.12)
         .build();
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1)
         .addCarver(Carvers.NETHER_CAVE)
         .addFeature(Decoration.VEGETAL_DECORATION, MiscOverworldPlacements.SPRING_LAVA);
      BiomeDefaultFeatures.addDefaultMushrooms($$3);
      $$3.addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_FIRE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_SOUL_FIRE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
         .addFeature(Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
         .addFeature(Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED)
         .addFeature(Decoration.VEGETAL_DECORATION, TreePlacements.WARPED_FUNGI)
         .addFeature(Decoration.VEGETAL_DECORATION, NetherPlacements.WARPED_FOREST_VEGETATION)
         .addFeature(Decoration.VEGETAL_DECORATION, NetherPlacements.NETHER_SPROUTS)
         .addFeature(Decoration.VEGETAL_DECORATION, NetherPlacements.TWISTING_VINES);
      BiomeDefaultFeatures.addNetherDefaultOres($$3);
      return baseBiome()
         .setAttribute(EnvironmentAttributes.FOG_COLOR, -15071974)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_WARPED_FOREST))
         .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(ParticleTypes.WARPED_SPORE, 0.01428F))
         .setAttribute(
            EnvironmentAttributes.AMBIENT_SOUNDS,
            new AmbientSounds(
               Optional.of(SoundEvents.AMBIENT_WARPED_FOREST_LOOP),
               Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_WARPED_FOREST_MOOD, 6000, 8, 2.0)),
               List.of(new AmbientAdditionsSettings(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111))
            )
         )
         .mobSpawnSettings($$2)
         .generationSettings($$3.build())
         .build();
   }
}
