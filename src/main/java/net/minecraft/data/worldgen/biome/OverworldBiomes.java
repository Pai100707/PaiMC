package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.FloatModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.Biome.TemperatureModifier;
import net.minecraft.world.level.biome.BiomeSpecialEffects.Builder;
import net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class OverworldBiomes {
   protected static final int NORMAL_WATER_COLOR = 4159204;
   private static final int DARK_DRY_FOLIAGE_COLOR = 8082228;
   public static final int SWAMP_SKELETON_WEIGHT = 70;

   public static int calculateSkyColor(float $$0) {
      float $$1 = $$0 / 3.0F;
      $$1 = Mth.clamp($$1, -1.0F, 1.0F);
      return ARGB.opaque(Mth.hsvToRgb(0.62222224F - $$1 * 0.05F, 0.5F + $$1 * 0.1F, 1.0F));
   }

   private static BiomeBuilder baseBiome(float $$0, float $$1) {
      return new BiomeBuilder()
         .hasPrecipitation(true)
         .temperature($$0)
         .downfall($$1)
         .setAttribute(EnvironmentAttributes.SKY_COLOR, calculateSkyColor($$0))
         .specialEffects(new Builder().waterColor(4159204).build());
   }

   private static void globalOverworldGeneration(net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$0) {
      BiomeDefaultFeatures.addDefaultCarversAndLakes($$0);
      BiomeDefaultFeatures.addDefaultCrystalFormations($$0);
      BiomeDefaultFeatures.addDefaultMonsterRoom($$0);
      BiomeDefaultFeatures.addDefaultUndergroundVariety($$0);
      BiomeDefaultFeatures.addDefaultSprings($$0);
      BiomeDefaultFeatures.addSurfaceFreezing($$0);
   }

   public static Biome oldGrowthTaiga(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals($$3);
      $$3.addSpawn(MobCategory.CREATURE, 8, new SpawnerData(EntityType.WOLF, 4, 4));
      $$3.addSpawn(MobCategory.CREATURE, 4, new SpawnerData(EntityType.RABBIT, 2, 3));
      $$3.addSpawn(MobCategory.CREATURE, 8, new SpawnerData(EntityType.FOX, 2, 4));
      if ($$2) {
         BiomeDefaultFeatures.commonSpawns($$3);
      } else {
         BiomeDefaultFeatures.caveSpawns($$3);
         BiomeDefaultFeatures.monsters($$3, 100, 25, 0, 100, false);
      }

      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$4 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$4);
      BiomeDefaultFeatures.addMossyStoneBlock($$4);
      BiomeDefaultFeatures.addFerns($$4);
      BiomeDefaultFeatures.addDefaultOres($$4);
      BiomeDefaultFeatures.addDefaultSoftDisks($$4);
      $$4.addFeature(Decoration.VEGETAL_DECORATION, $$2 ? VegetationPlacements.TREES_OLD_GROWTH_SPRUCE_TAIGA : VegetationPlacements.TREES_OLD_GROWTH_PINE_TAIGA);
      BiomeDefaultFeatures.addDefaultFlowers($$4);
      BiomeDefaultFeatures.addGiantTaigaVegetation($$4);
      BiomeDefaultFeatures.addDefaultMushrooms($$4);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$4, true);
      BiomeDefaultFeatures.addCommonBerryBushes($$4);
      return baseBiome($$2 ? 0.25F : 0.3F, 0.8F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_OLD_GROWTH_TAIGA))
         .mobSpawnSettings($$3.build())
         .generationSettings($$4.build())
         .build();
   }

   public static Biome sparseJungle(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns($$2);
      $$2.addSpawn(MobCategory.CREATURE, 8, new SpawnerData(EntityType.WOLF, 2, 4));
      return baseJungle($$0, $$1, 0.8F, false, true, false)
         .mobSpawnSettings($$2.build())
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_SPARSE_JUNGLE))
         .build();
   }

   public static Biome jungle(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns($$2);
      $$2.addSpawn(MobCategory.CREATURE, 40, new SpawnerData(EntityType.PARROT, 1, 2))
         .addSpawn(MobCategory.MONSTER, 2, new SpawnerData(EntityType.OCELOT, 1, 3))
         .addSpawn(MobCategory.CREATURE, 1, new SpawnerData(EntityType.PANDA, 1, 2));
      return baseJungle($$0, $$1, 0.9F, false, false, true)
         .mobSpawnSettings($$2.build())
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_JUNGLE))
         .setAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, true)
         .build();
   }

   public static Biome bambooJungle(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns($$2);
      $$2.addSpawn(MobCategory.CREATURE, 40, new SpawnerData(EntityType.PARROT, 1, 2))
         .addSpawn(MobCategory.CREATURE, 80, new SpawnerData(EntityType.PANDA, 1, 2))
         .addSpawn(MobCategory.MONSTER, 2, new SpawnerData(EntityType.OCELOT, 1, 1));
      return baseJungle($$0, $$1, 0.9F, true, false, true)
         .mobSpawnSettings($$2.build())
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_BAMBOO_JUNGLE))
         .setAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, true)
         .build();
   }

   private static BiomeBuilder baseJungle(
      HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, float $$2, boolean $$3, boolean $$4, boolean $$5
   ) {
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$6 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$6);
      BiomeDefaultFeatures.addDefaultOres($$6);
      BiomeDefaultFeatures.addDefaultSoftDisks($$6);
      if ($$3) {
         BiomeDefaultFeatures.addBambooVegetation($$6);
      } else {
         if ($$5) {
            BiomeDefaultFeatures.addLightBambooVegetation($$6);
         }

         if ($$4) {
            BiomeDefaultFeatures.addSparseJungleTrees($$6);
         } else {
            BiomeDefaultFeatures.addJungleTrees($$6);
         }
      }

      BiomeDefaultFeatures.addWarmFlowers($$6);
      BiomeDefaultFeatures.addJungleGrass($$6);
      BiomeDefaultFeatures.addDefaultMushrooms($$6);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$6, true);
      BiomeDefaultFeatures.addJungleVines($$6);
      if ($$4) {
         BiomeDefaultFeatures.addSparseJungleMelons($$6);
      } else {
         BiomeDefaultFeatures.addJungleMelons($$6);
      }

      return baseBiome(0.95F, $$2).generationSettings($$6.build());
   }

   public static Biome windsweptHills(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals($$3);
      $$3.addSpawn(MobCategory.CREATURE, 5, new SpawnerData(EntityType.LLAMA, 4, 6));
      BiomeDefaultFeatures.commonSpawns($$3);
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$4 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$4);
      BiomeDefaultFeatures.addDefaultOres($$4);
      BiomeDefaultFeatures.addDefaultSoftDisks($$4);
      if ($$2) {
         BiomeDefaultFeatures.addMountainForestTrees($$4);
      } else {
         BiomeDefaultFeatures.addMountainTrees($$4);
      }

      BiomeDefaultFeatures.addBushes($$4);
      BiomeDefaultFeatures.addDefaultFlowers($$4);
      BiomeDefaultFeatures.addDefaultGrass($$4);
      BiomeDefaultFeatures.addDefaultMushrooms($$4);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$4, true);
      BiomeDefaultFeatures.addExtraEmeralds($$4);
      BiomeDefaultFeatures.addInfestedStone($$4);
      return baseBiome(0.2F, 0.3F).mobSpawnSettings($$3.build()).generationSettings($$4.build()).build();
   }

   public static Biome desert(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.desertSpawns($$2);
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      BiomeDefaultFeatures.addFossilDecoration($$3);
      globalOverworldGeneration($$3);
      BiomeDefaultFeatures.addDefaultOres($$3);
      BiomeDefaultFeatures.addDefaultSoftDisks($$3);
      BiomeDefaultFeatures.addDefaultFlowers($$3);
      BiomeDefaultFeatures.addDefaultGrass($$3);
      BiomeDefaultFeatures.addDesertVegetation($$3);
      BiomeDefaultFeatures.addDefaultMushrooms($$3);
      BiomeDefaultFeatures.addDesertExtraVegetation($$3);
      BiomeDefaultFeatures.addDesertExtraDecoration($$3);
      return baseBiome(2.0F, 0.0F)
         .hasPrecipitation(false)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_DESERT))
         .setAttribute(EnvironmentAttributes.SNOW_GOLEM_MELTS, true)
         .mobSpawnSettings($$2.build())
         .generationSettings($$3.build())
         .build();
   }

   public static Biome plains(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2, boolean $$3, boolean $$4) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$5 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$6 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$6);
      if ($$3) {
         $$5.creatureGenerationProbability(0.07F);
         BiomeDefaultFeatures.snowySpawns($$5, !$$4);
         if ($$4) {
            $$6.addFeature(Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_SPIKE);
            $$6.addFeature(Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_PATCH);
         }
      } else {
         BiomeDefaultFeatures.plainsSpawns($$5);
         BiomeDefaultFeatures.addPlainGrass($$6);
         if ($$2) {
            $$6.addFeature(Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUNFLOWER);
         } else {
            BiomeDefaultFeatures.addBushes($$6);
         }
      }

      BiomeDefaultFeatures.addDefaultOres($$6);
      BiomeDefaultFeatures.addDefaultSoftDisks($$6);
      if ($$3) {
         BiomeDefaultFeatures.addSnowyTrees($$6);
         BiomeDefaultFeatures.addDefaultFlowers($$6);
         BiomeDefaultFeatures.addDefaultGrass($$6);
      } else {
         BiomeDefaultFeatures.addPlainVegetation($$6);
      }

      BiomeDefaultFeatures.addDefaultMushrooms($$6);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$6, true);
      return baseBiome($$3 ? 0.0F : 0.8F, $$3 ? 0.5F : 0.4F).mobSpawnSettings($$5.build()).generationSettings($$6.build()).build();
   }

   public static Biome mushroomFields(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.mooshroomSpawns($$2);
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$3);
      BiomeDefaultFeatures.addDefaultOres($$3);
      BiomeDefaultFeatures.addDefaultSoftDisks($$3);
      BiomeDefaultFeatures.addMushroomFieldVegetation($$3);
      BiomeDefaultFeatures.addNearWaterVegetation($$3);
      return baseBiome(0.9F, 1.0F)
         .setAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, true)
         .setAttribute(EnvironmentAttributes.CAN_PILLAGER_PATROL_SPAWN, false)
         .mobSpawnSettings($$2.build())
         .generationSettings($$3.build())
         .build();
   }

   public static Biome savanna(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2, boolean $$3) {
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$4 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$4);
      if (!$$2) {
         BiomeDefaultFeatures.addSavannaGrass($$4);
      }

      BiomeDefaultFeatures.addDefaultOres($$4);
      BiomeDefaultFeatures.addDefaultSoftDisks($$4);
      if ($$2) {
         BiomeDefaultFeatures.addShatteredSavannaTrees($$4);
         BiomeDefaultFeatures.addDefaultFlowers($$4);
         BiomeDefaultFeatures.addShatteredSavannaGrass($$4);
      } else {
         BiomeDefaultFeatures.addSavannaTrees($$4);
         BiomeDefaultFeatures.addWarmFlowers($$4);
         BiomeDefaultFeatures.addSavannaExtraGrass($$4);
      }

      BiomeDefaultFeatures.addDefaultMushrooms($$4);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$4, true);
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$5 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals($$5);
      $$5.addSpawn(MobCategory.CREATURE, 1, new SpawnerData(EntityType.HORSE, 2, 6))
         .addSpawn(MobCategory.CREATURE, 1, new SpawnerData(EntityType.DONKEY, 1, 1))
         .addSpawn(MobCategory.CREATURE, 10, new SpawnerData(EntityType.ARMADILLO, 2, 3));
      BiomeDefaultFeatures.commonSpawnWithZombieHorse($$5);
      if ($$3) {
         $$5.addSpawn(MobCategory.CREATURE, 8, new SpawnerData(EntityType.LLAMA, 4, 4));
         $$5.addSpawn(MobCategory.CREATURE, 8, new SpawnerData(EntityType.WOLF, 4, 8));
      }

      return baseBiome(2.0F, 0.0F)
         .hasPrecipitation(false)
         .setAttribute(EnvironmentAttributes.SNOW_GOLEM_MELTS, true)
         .mobSpawnSettings($$5.build())
         .generationSettings($$4.build())
         .build();
   }

   public static Biome badlands(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals($$3);
      BiomeDefaultFeatures.commonSpawns($$3);
      $$3.addSpawn(MobCategory.CREATURE, 6, new SpawnerData(EntityType.ARMADILLO, 1, 2));
      $$3.creatureGenerationProbability(0.03F);
      if ($$2) {
         $$3.addSpawn(MobCategory.CREATURE, 2, new SpawnerData(EntityType.WOLF, 4, 8));
         $$3.creatureGenerationProbability(0.04F);
      }

      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$4 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$4);
      BiomeDefaultFeatures.addDefaultOres($$4);
      BiomeDefaultFeatures.addExtraGold($$4);
      BiomeDefaultFeatures.addDefaultSoftDisks($$4);
      if ($$2) {
         BiomeDefaultFeatures.addBadlandsTrees($$4);
      }

      BiomeDefaultFeatures.addBadlandGrass($$4);
      BiomeDefaultFeatures.addDefaultMushrooms($$4);
      BiomeDefaultFeatures.addBadlandExtraVegetation($$4);
      return baseBiome(2.0F, 0.0F)
         .hasPrecipitation(false)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_BADLANDS))
         .setAttribute(EnvironmentAttributes.SNOW_GOLEM_MELTS, true)
         .specialEffects(new Builder().waterColor(4159204).foliageColorOverride(10387789).grassColorOverride(9470285).build())
         .mobSpawnSettings($$3.build())
         .generationSettings($$4.build())
         .build();
   }

   private static BiomeBuilder baseOcean() {
      return baseBiome(0.5F, 0.5F).setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD.withUnderwater(Musics.UNDER_WATER));
   }

   private static net.minecraft.world.level.biome.BiomeGenerationSettings.Builder baseOceanGeneration(
      HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1
   ) {
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$2 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$2);
      BiomeDefaultFeatures.addDefaultOres($$2);
      BiomeDefaultFeatures.addDefaultSoftDisks($$2);
      BiomeDefaultFeatures.addWaterTrees($$2);
      BiomeDefaultFeatures.addDefaultFlowers($$2);
      BiomeDefaultFeatures.addDefaultGrass($$2);
      BiomeDefaultFeatures.addDefaultMushrooms($$2);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$2, true);
      return $$2;
   }

   public static Biome coldOcean(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.oceanSpawns($$3, 3, 4, 15);
      $$3.addSpawn(MobCategory.WATER_AMBIENT, 15, new SpawnerData(EntityType.SALMON, 1, 5));
      $$3.addSpawn(MobCategory.WATER_CREATURE, 2, new SpawnerData(EntityType.NAUTILUS, 1, 1));
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$4 = baseOceanGeneration($$0, $$1);
      $$4.addFeature(Decoration.VEGETAL_DECORATION, $$2 ? AquaticPlacements.SEAGRASS_DEEP_COLD : AquaticPlacements.SEAGRASS_COLD);
      BiomeDefaultFeatures.addColdOceanExtraVegetation($$4);
      return baseOcean().specialEffects(new Builder().waterColor(4020182).build()).mobSpawnSettings($$3.build()).generationSettings($$4.build()).build();
   }

   public static Biome ocean(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.oceanSpawns($$3, 1, 4, 10);
      $$3.addSpawn(MobCategory.WATER_CREATURE, 1, new SpawnerData(EntityType.DOLPHIN, 1, 2))
         .addSpawn(MobCategory.WATER_CREATURE, 5, new SpawnerData(EntityType.NAUTILUS, 1, 1));
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$4 = baseOceanGeneration($$0, $$1);
      $$4.addFeature(Decoration.VEGETAL_DECORATION, $$2 ? AquaticPlacements.SEAGRASS_DEEP : AquaticPlacements.SEAGRASS_NORMAL);
      BiomeDefaultFeatures.addColdOceanExtraVegetation($$4);
      return baseOcean().mobSpawnSettings($$3.build()).generationSettings($$4.build()).build();
   }

   public static Biome lukeWarmOcean(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      if ($$2) {
         BiomeDefaultFeatures.oceanSpawns($$3, 8, 4, 8);
      } else {
         BiomeDefaultFeatures.oceanSpawns($$3, 10, 2, 15);
      }

      $$3.addSpawn(MobCategory.WATER_AMBIENT, 5, new SpawnerData(EntityType.PUFFERFISH, 1, 3))
         .addSpawn(MobCategory.WATER_AMBIENT, 25, new SpawnerData(EntityType.TROPICAL_FISH, 8, 8))
         .addSpawn(MobCategory.WATER_CREATURE, 2, new SpawnerData(EntityType.DOLPHIN, 1, 2))
         .addSpawn(MobCategory.WATER_CREATURE, 5, new SpawnerData(EntityType.NAUTILUS, 1, 1));
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$4 = baseOceanGeneration($$0, $$1);
      $$4.addFeature(Decoration.VEGETAL_DECORATION, $$2 ? AquaticPlacements.SEAGRASS_DEEP_WARM : AquaticPlacements.SEAGRASS_WARM);
      BiomeDefaultFeatures.addLukeWarmKelp($$4);
      return baseOcean()
         .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, -16509389)
         .specialEffects(new Builder().waterColor(4566514).build())
         .mobSpawnSettings($$3.build())
         .generationSettings($$4.build())
         .build();
   }

   public static Biome warmOcean(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder()
         .addSpawn(MobCategory.WATER_AMBIENT, 15, new SpawnerData(EntityType.PUFFERFISH, 1, 3))
         .addSpawn(MobCategory.WATER_CREATURE, 5, new SpawnerData(EntityType.NAUTILUS, 1, 1));
      BiomeDefaultFeatures.warmOceanSpawns($$2, 10, 4);
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = baseOceanGeneration($$0, $$1)
         .addFeature(Decoration.VEGETAL_DECORATION, AquaticPlacements.WARM_OCEAN_VEGETATION)
         .addFeature(Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_WARM)
         .addFeature(Decoration.VEGETAL_DECORATION, AquaticPlacements.SEA_PICKLE);
      return baseOcean()
         .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, -16507085)
         .specialEffects(new Builder().waterColor(4445678).build())
         .mobSpawnSettings($$2.build())
         .generationSettings($$3.build())
         .build();
   }

   public static Biome frozenOcean(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder()
         .addSpawn(MobCategory.WATER_CREATURE, 1, new SpawnerData(EntityType.SQUID, 1, 4))
         .addSpawn(MobCategory.WATER_AMBIENT, 15, new SpawnerData(EntityType.SALMON, 1, 5))
         .addSpawn(MobCategory.CREATURE, 1, new SpawnerData(EntityType.POLAR_BEAR, 1, 2))
         .addSpawn(MobCategory.WATER_CREATURE, 2, new SpawnerData(EntityType.NAUTILUS, 1, 1));
      BiomeDefaultFeatures.commonSpawns($$3);
      $$3.addSpawn(MobCategory.MONSTER, 5, new SpawnerData(EntityType.DROWNED, 1, 1));
      float $$4 = $$2 ? 0.5F : 0.0F;
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$5 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      BiomeDefaultFeatures.addIcebergs($$5);
      globalOverworldGeneration($$5);
      BiomeDefaultFeatures.addBlueIce($$5);
      BiomeDefaultFeatures.addDefaultOres($$5);
      BiomeDefaultFeatures.addDefaultSoftDisks($$5);
      BiomeDefaultFeatures.addWaterTrees($$5);
      BiomeDefaultFeatures.addDefaultFlowers($$5);
      BiomeDefaultFeatures.addDefaultGrass($$5);
      BiomeDefaultFeatures.addDefaultMushrooms($$5);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$5, true);
      return baseBiome($$4, 0.5F)
         .temperatureAdjustment(TemperatureModifier.FROZEN)
         .specialEffects(new Builder().waterColor(3750089).build())
         .mobSpawnSettings($$3.build())
         .generationSettings($$5.build())
         .build();
   }

   public static Biome forest(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2, boolean $$3, boolean $$4) {
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$5 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$5);
      BackgroundMusic $$6;
      if ($$4) {
         $$6 = new BackgroundMusic(SoundEvents.MUSIC_BIOME_FLOWER_FOREST);
         $$5.addFeature(Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FOREST_FLOWERS);
      } else {
         $$6 = new BackgroundMusic(SoundEvents.MUSIC_BIOME_FOREST);
         BiomeDefaultFeatures.addForestFlowers($$5);
      }

      BiomeDefaultFeatures.addDefaultOres($$5);
      BiomeDefaultFeatures.addDefaultSoftDisks($$5);
      if ($$4) {
         $$5.addFeature(Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_FLOWER_FOREST);
         $$5.addFeature(Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FLOWER_FOREST);
         BiomeDefaultFeatures.addDefaultGrass($$5);
      } else {
         if ($$2) {
            BiomeDefaultFeatures.addBirchForestFlowers($$5);
            if ($$3) {
               BiomeDefaultFeatures.addTallBirchTrees($$5);
            } else {
               BiomeDefaultFeatures.addBirchTrees($$5);
            }
         } else {
            BiomeDefaultFeatures.addOtherBirchTrees($$5);
         }

         BiomeDefaultFeatures.addBushes($$5);
         BiomeDefaultFeatures.addDefaultFlowers($$5);
         BiomeDefaultFeatures.addForestGrass($$5);
      }

      BiomeDefaultFeatures.addDefaultMushrooms($$5);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$5, true);
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$8 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals($$8);
      BiomeDefaultFeatures.commonSpawns($$8);
      if ($$4) {
         $$8.addSpawn(MobCategory.CREATURE, 4, new SpawnerData(EntityType.RABBIT, 2, 3));
      } else if (!$$2) {
         $$8.addSpawn(MobCategory.CREATURE, 5, new SpawnerData(EntityType.WOLF, 4, 4));
      }

      return baseBiome($$2 ? 0.6F : 0.7F, $$2 ? 0.6F : 0.8F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, $$6)
         .mobSpawnSettings($$8.build())
         .generationSettings($$5.build())
         .build();
   }

   public static Biome taiga(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals($$3);
      $$3.addSpawn(MobCategory.CREATURE, 8, new SpawnerData(EntityType.WOLF, 4, 4))
         .addSpawn(MobCategory.CREATURE, 4, new SpawnerData(EntityType.RABBIT, 2, 3))
         .addSpawn(MobCategory.CREATURE, 8, new SpawnerData(EntityType.FOX, 2, 4));
      BiomeDefaultFeatures.commonSpawns($$3);
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$4 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$4);
      BiomeDefaultFeatures.addFerns($$4);
      BiomeDefaultFeatures.addDefaultOres($$4);
      BiomeDefaultFeatures.addDefaultSoftDisks($$4);
      BiomeDefaultFeatures.addTaigaTrees($$4);
      BiomeDefaultFeatures.addDefaultFlowers($$4);
      BiomeDefaultFeatures.addTaigaGrass($$4);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$4, true);
      if ($$2) {
         BiomeDefaultFeatures.addRareBerryBushes($$4);
      } else {
         BiomeDefaultFeatures.addCommonBerryBushes($$4);
      }

      int $$5 = $$2 ? 4020182 : 4159204;
      return baseBiome($$2 ? -0.5F : 0.25F, $$2 ? 0.4F : 0.8F)
         .specialEffects(new Builder().waterColor($$5).build())
         .mobSpawnSettings($$3.build())
         .generationSettings($$4.build())
         .build();
   }

   public static Biome darkForest(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      if (!$$2) {
         BiomeDefaultFeatures.farmAnimals($$3);
      }

      BiomeDefaultFeatures.commonSpawns($$3);
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$4 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$4);
      $$4.addFeature(Decoration.VEGETAL_DECORATION, $$2 ? VegetationPlacements.PALE_GARDEN_VEGETATION : VegetationPlacements.DARK_FOREST_VEGETATION);
      if (!$$2) {
         BiomeDefaultFeatures.addForestFlowers($$4);
      } else {
         $$4.addFeature(Decoration.VEGETAL_DECORATION, VegetationPlacements.PALE_MOSS_PATCH);
         $$4.addFeature(Decoration.VEGETAL_DECORATION, VegetationPlacements.PALE_GARDEN_FLOWERS);
      }

      BiomeDefaultFeatures.addDefaultOres($$4);
      BiomeDefaultFeatures.addDefaultSoftDisks($$4);
      if (!$$2) {
         BiomeDefaultFeatures.addDefaultFlowers($$4);
      } else {
         $$4.addFeature(Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_PALE_GARDEN);
      }

      BiomeDefaultFeatures.addForestGrass($$4);
      if (!$$2) {
         BiomeDefaultFeatures.addDefaultMushrooms($$4);
         BiomeDefaultFeatures.addLeafLitterPatch($$4);
      }

      BiomeDefaultFeatures.addDefaultExtraVegetation($$4, true);
      EnvironmentAttributeMap $$5 = EnvironmentAttributeMap.builder()
         .set(EnvironmentAttributes.SKY_COLOR, -4605511)
         .set(EnvironmentAttributes.FOG_COLOR, -8292496)
         .set(EnvironmentAttributes.WATER_FOG_COLOR, -11179648)
         .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.EMPTY)
         .set(EnvironmentAttributes.MUSIC_VOLUME, 0.0F)
         .build();
      EnvironmentAttributeMap $$6 = EnvironmentAttributeMap.builder()
         .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_FOREST))
         .build();
      return baseBiome(0.7F, 0.8F)
         .putAttributes($$2 ? $$5 : $$6)
         .specialEffects(
            $$2
               ? new Builder().waterColor(7768221).grassColorOverride(7832178).foliageColorOverride(8883574).dryFoliageColorOverride(10528412).build()
               : new Builder().waterColor(4159204).dryFoliageColorOverride(8082228).grassColorModifier(GrassColorModifier.DARK_FOREST).build()
         )
         .mobSpawnSettings($$3.build())
         .generationSettings($$4.build())
         .build();
   }

   public static Biome swamp(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals($$2);
      BiomeDefaultFeatures.swampSpawns($$2, 70);
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      BiomeDefaultFeatures.addFossilDecoration($$3);
      globalOverworldGeneration($$3);
      BiomeDefaultFeatures.addDefaultOres($$3);
      BiomeDefaultFeatures.addSwampClayDisk($$3);
      BiomeDefaultFeatures.addSwampVegetation($$3);
      BiomeDefaultFeatures.addDefaultMushrooms($$3);
      BiomeDefaultFeatures.addSwampExtraVegetation($$3);
      $$3.addFeature(Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
      return baseBiome(0.8F, 0.9F)
         .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, -14474473)
         .modifyAttribute(EnvironmentAttributes.WATER_FOG_END_DISTANCE, FloatModifier.MULTIPLY, 0.85F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_SWAMP))
         .setAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, true)
         .specialEffects(
            new Builder()
               .waterColor(6388580)
               .foliageColorOverride(6975545)
               .dryFoliageColorOverride(8082228)
               .grassColorModifier(GrassColorModifier.SWAMP)
               .build()
         )
         .mobSpawnSettings($$2.build())
         .generationSettings($$3.build())
         .build();
   }

   public static Biome mangroveSwamp(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.swampSpawns($$2, 70);
      $$2.addSpawn(MobCategory.WATER_AMBIENT, 25, new SpawnerData(EntityType.TROPICAL_FISH, 8, 8));
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      BiomeDefaultFeatures.addFossilDecoration($$3);
      globalOverworldGeneration($$3);
      BiomeDefaultFeatures.addDefaultOres($$3);
      BiomeDefaultFeatures.addMangroveSwampDisks($$3);
      BiomeDefaultFeatures.addMangroveSwampVegetation($$3);
      BiomeDefaultFeatures.addMangroveSwampExtraVegetation($$3);
      return baseBiome(0.8F, 0.9F)
         .setAttribute(EnvironmentAttributes.FOG_COLOR, -4138753)
         .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, -11699616)
         .modifyAttribute(EnvironmentAttributes.WATER_FOG_END_DISTANCE, FloatModifier.MULTIPLY, 0.85F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_SWAMP))
         .setAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, true)
         .specialEffects(
            new Builder()
               .waterColor(3832426)
               .foliageColorOverride(9285927)
               .dryFoliageColorOverride(8082228)
               .grassColorModifier(GrassColorModifier.SWAMP)
               .build()
         )
         .mobSpawnSettings($$2.build())
         .generationSettings($$3.build())
         .build();
   }

   public static Biome river(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder()
         .addSpawn(MobCategory.WATER_CREATURE, 2, new SpawnerData(EntityType.SQUID, 1, 4))
         .addSpawn(MobCategory.WATER_AMBIENT, 5, new SpawnerData(EntityType.SALMON, 1, 5));
      BiomeDefaultFeatures.commonSpawns($$3);
      $$3.addSpawn(MobCategory.MONSTER, $$2 ? 1 : 100, new SpawnerData(EntityType.DROWNED, 1, 1));
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$4 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$4);
      BiomeDefaultFeatures.addDefaultOres($$4);
      BiomeDefaultFeatures.addDefaultSoftDisks($$4);
      BiomeDefaultFeatures.addWaterTrees($$4);
      BiomeDefaultFeatures.addBushes($$4);
      BiomeDefaultFeatures.addDefaultFlowers($$4);
      BiomeDefaultFeatures.addDefaultGrass($$4);
      BiomeDefaultFeatures.addDefaultMushrooms($$4);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$4, true);
      if (!$$2) {
         $$4.addFeature(Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_RIVER);
      }

      return baseBiome($$2 ? 0.0F : 0.5F, 0.5F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD.withUnderwater(Musics.UNDER_WATER))
         .specialEffects(new Builder().waterColor($$2 ? 3750089 : 4159204).build())
         .mobSpawnSettings($$3.build())
         .generationSettings($$4.build())
         .build();
   }

   public static Biome beach(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2, boolean $$3) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$4 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      boolean $$5 = !$$3 && !$$2;
      if ($$5) {
         $$4.addSpawn(MobCategory.CREATURE, 5, new SpawnerData(EntityType.TURTLE, 2, 5));
      }

      BiomeDefaultFeatures.commonSpawns($$4);
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$6 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$6);
      BiomeDefaultFeatures.addDefaultOres($$6);
      BiomeDefaultFeatures.addDefaultSoftDisks($$6);
      BiomeDefaultFeatures.addDefaultFlowers($$6);
      BiomeDefaultFeatures.addDefaultGrass($$6);
      BiomeDefaultFeatures.addDefaultMushrooms($$6);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$6, true);
      float $$7;
      if ($$2) {
         $$7 = 0.05F;
      } else if ($$3) {
         $$7 = 0.2F;
      } else {
         $$7 = 0.8F;
      }

      int $$10 = $$2 ? 4020182 : 4159204;
      return baseBiome($$7, $$5 ? 0.4F : 0.3F)
         .specialEffects(new Builder().waterColor($$10).build())
         .mobSpawnSettings($$4.build())
         .generationSettings($$6.build())
         .build();
   }

   public static Biome theVoid(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$2 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      $$2.addFeature(Decoration.TOP_LAYER_MODIFICATION, MiscOverworldPlacements.VOID_START_PLATFORM);
      return baseBiome(0.5F, 0.5F)
         .hasPrecipitation(false)
         .mobSpawnSettings(new net.minecraft.world.level.biome.MobSpawnSettings.Builder().build())
         .generationSettings($$2.build())
         .build();
   }

   public static Biome meadowOrCherryGrove(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1, boolean $$2) {
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$4 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      $$4.addSpawn(MobCategory.CREATURE, 1, new SpawnerData($$2 ? EntityType.PIG : EntityType.DONKEY, 1, 2))
         .addSpawn(MobCategory.CREATURE, 2, new SpawnerData(EntityType.RABBIT, 2, 6))
         .addSpawn(MobCategory.CREATURE, 2, new SpawnerData(EntityType.SHEEP, 2, 4));
      BiomeDefaultFeatures.commonSpawns($$4);
      globalOverworldGeneration($$3);
      BiomeDefaultFeatures.addPlainGrass($$3);
      BiomeDefaultFeatures.addDefaultOres($$3);
      BiomeDefaultFeatures.addDefaultSoftDisks($$3);
      if ($$2) {
         BiomeDefaultFeatures.addCherryGroveVegetation($$3);
      } else {
         BiomeDefaultFeatures.addMeadowVegetation($$3);
      }

      BiomeDefaultFeatures.addExtraEmeralds($$3);
      BiomeDefaultFeatures.addInfestedStone($$3);
      if ($$2) {
         Builder $$5 = new Builder().waterColor(6141935).grassColorOverride(11983713).foliageColorOverride(11983713);
         return baseBiome(0.5F, 0.8F)
            .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, -10635281)
            .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_CHERRY_GROVE))
            .specialEffects($$5.build())
            .mobSpawnSettings($$4.build())
            .generationSettings($$3.build())
            .build();
      } else {
         return baseBiome(0.5F, 0.8F)
            .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_MEADOW))
            .specialEffects(new Builder().waterColor(937679).build())
            .mobSpawnSettings($$4.build())
            .generationSettings($$3.build())
            .build();
      }
   }

   private static BiomeBuilder basePeaks(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$2 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      $$3.addSpawn(MobCategory.CREATURE, 5, new SpawnerData(EntityType.GOAT, 1, 3));
      BiomeDefaultFeatures.commonSpawns($$3);
      globalOverworldGeneration($$2);
      BiomeDefaultFeatures.addFrozenSprings($$2);
      BiomeDefaultFeatures.addDefaultOres($$2);
      BiomeDefaultFeatures.addDefaultSoftDisks($$2);
      BiomeDefaultFeatures.addExtraEmeralds($$2);
      BiomeDefaultFeatures.addInfestedStone($$2);
      return baseBiome(-0.7F, 0.9F)
         .setAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, true)
         .mobSpawnSettings($$3.build())
         .generationSettings($$2.build());
   }

   public static Biome frozenPeaks(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      return basePeaks($$0, $$1).setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_FROZEN_PEAKS)).build();
   }

   public static Biome jaggedPeaks(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      return basePeaks($$0, $$1).setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_JAGGED_PEAKS)).build();
   }

   public static Biome stonyPeaks(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$2 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns($$3);
      globalOverworldGeneration($$2);
      BiomeDefaultFeatures.addDefaultOres($$2);
      BiomeDefaultFeatures.addDefaultSoftDisks($$2);
      BiomeDefaultFeatures.addExtraEmeralds($$2);
      BiomeDefaultFeatures.addInfestedStone($$2);
      return baseBiome(1.0F, 0.3F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_STONY_PEAKS))
         .mobSpawnSettings($$3.build())
         .generationSettings($$2.build())
         .build();
   }

   public static Biome snowySlopes(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$2 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      $$3.addSpawn(MobCategory.CREATURE, 4, new SpawnerData(EntityType.RABBIT, 2, 3)).addSpawn(MobCategory.CREATURE, 5, new SpawnerData(EntityType.GOAT, 1, 3));
      BiomeDefaultFeatures.commonSpawns($$3);
      globalOverworldGeneration($$2);
      BiomeDefaultFeatures.addFrozenSprings($$2);
      BiomeDefaultFeatures.addDefaultOres($$2);
      BiomeDefaultFeatures.addDefaultSoftDisks($$2);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$2, false);
      BiomeDefaultFeatures.addExtraEmeralds($$2);
      BiomeDefaultFeatures.addInfestedStone($$2);
      return baseBiome(-0.3F, 0.9F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_SNOWY_SLOPES))
         .setAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, true)
         .mobSpawnSettings($$3.build())
         .generationSettings($$2.build())
         .build();
   }

   public static Biome grove(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$2 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$3 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      $$3.addSpawn(MobCategory.CREATURE, 1, new SpawnerData(EntityType.WOLF, 1, 1))
         .addSpawn(MobCategory.CREATURE, 8, new SpawnerData(EntityType.RABBIT, 2, 3))
         .addSpawn(MobCategory.CREATURE, 4, new SpawnerData(EntityType.FOX, 2, 4));
      BiomeDefaultFeatures.commonSpawns($$3);
      globalOverworldGeneration($$2);
      BiomeDefaultFeatures.addFrozenSprings($$2);
      BiomeDefaultFeatures.addDefaultOres($$2);
      BiomeDefaultFeatures.addDefaultSoftDisks($$2);
      BiomeDefaultFeatures.addGroveTrees($$2);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$2, false);
      BiomeDefaultFeatures.addExtraEmeralds($$2);
      BiomeDefaultFeatures.addInfestedStone($$2);
      return baseBiome(-0.2F, 0.8F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_GROVE))
         .mobSpawnSettings($$3.build())
         .generationSettings($$2.build())
         .build();
   }

   public static Biome lushCaves(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      $$2.addSpawn(MobCategory.AXOLOTLS, 10, new SpawnerData(EntityType.AXOLOTL, 4, 6));
      $$2.addSpawn(MobCategory.WATER_AMBIENT, 25, new SpawnerData(EntityType.TROPICAL_FISH, 8, 8));
      BiomeDefaultFeatures.commonSpawns($$2);
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$3);
      BiomeDefaultFeatures.addPlainGrass($$3);
      BiomeDefaultFeatures.addDefaultOres($$3);
      BiomeDefaultFeatures.addLushCavesSpecialOres($$3);
      BiomeDefaultFeatures.addDefaultSoftDisks($$3);
      BiomeDefaultFeatures.addLushCavesVegetationFeatures($$3);
      return baseBiome(0.5F, 0.5F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES))
         .mobSpawnSettings($$2.build())
         .generationSettings($$3.build())
         .build();
   }

   public static Biome dripstoneCaves(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      BiomeDefaultFeatures.dripstoneCavesSpawns($$2);
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      globalOverworldGeneration($$3);
      BiomeDefaultFeatures.addPlainGrass($$3);
      BiomeDefaultFeatures.addDefaultOres($$3, true);
      BiomeDefaultFeatures.addDefaultSoftDisks($$3);
      BiomeDefaultFeatures.addPlainVegetation($$3);
      BiomeDefaultFeatures.addDefaultMushrooms($$3);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$3, false);
      BiomeDefaultFeatures.addDripstone($$3);
      return baseBiome(0.8F, 0.4F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_DRIPSTONE_CAVES))
         .mobSpawnSettings($$2.build())
         .generationSettings($$3.build())
         .build();
   }

   public static Biome deepDark(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
      net.minecraft.world.level.biome.MobSpawnSettings.Builder $$2 = new net.minecraft.world.level.biome.MobSpawnSettings.Builder();
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder $$3 = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder($$0, $$1);
      $$3.addCarver(Carvers.CAVE);
      $$3.addCarver(Carvers.CAVE_EXTRA_UNDERGROUND);
      $$3.addCarver(Carvers.CANYON);
      BiomeDefaultFeatures.addDefaultCrystalFormations($$3);
      BiomeDefaultFeatures.addDefaultMonsterRoom($$3);
      BiomeDefaultFeatures.addDefaultUndergroundVariety($$3);
      BiomeDefaultFeatures.addSurfaceFreezing($$3);
      BiomeDefaultFeatures.addPlainGrass($$3);
      BiomeDefaultFeatures.addDefaultOres($$3);
      BiomeDefaultFeatures.addDefaultSoftDisks($$3);
      BiomeDefaultFeatures.addPlainVegetation($$3);
      BiomeDefaultFeatures.addDefaultMushrooms($$3);
      BiomeDefaultFeatures.addDefaultExtraVegetation($$3, false);
      BiomeDefaultFeatures.addSculk($$3);
      return baseBiome(0.8F, 0.4F)
         .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_DEEP_DARK))
         .mobSpawnSettings($$2.build())
         .generationSettings($$3.build())
         .build();
   }
}
