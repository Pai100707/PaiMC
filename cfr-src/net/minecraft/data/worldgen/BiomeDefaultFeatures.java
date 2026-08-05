/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen;

import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeDefaultFeatures {
    public static void addDefaultCarversAndLakes(BiomeGenerationSettings.Builder $$0) {
        $$0.addCarver(Carvers.CAVE);
        $$0.addCarver(Carvers.CAVE_EXTRA_UNDERGROUND);
        $$0.addCarver(Carvers.CANYON);
        $$0.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND);
        $$0.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_SURFACE);
    }

    public static void addDefaultMonsterRoom(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.MONSTER_ROOM);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.MONSTER_ROOM_DEEP);
    }

    public static void addDefaultUndergroundVariety(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIRT);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GRAVEL);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GRANITE_UPPER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GRANITE_LOWER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIORITE_UPPER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIORITE_LOWER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_ANDESITE_UPPER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_ANDESITE_LOWER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_TUFF);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.GLOW_LICHEN);
    }

    public static void addDripstone(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.LARGE_DRIPSTONE);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.DRIPSTONE_CLUSTER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.POINTED_DRIPSTONE);
    }

    public static void addSculk(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.SCULK_VEIN);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.SCULK_PATCH_DEEP_DARK);
    }

    public static void addDefaultOres(BiomeGenerationSettings.Builder $$0) {
        BiomeDefaultFeatures.addDefaultOres($$0, false);
    }

    public static void addDefaultOres(BiomeGenerationSettings.Builder $$0, boolean $$1) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_COAL_UPPER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_COAL_LOWER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_IRON_UPPER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_IRON_MIDDLE);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_IRON_SMALL);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GOLD);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GOLD_LOWER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_REDSTONE);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_REDSTONE_LOWER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIAMOND);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIAMOND_MEDIUM);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIAMOND_LARGE);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIAMOND_BURIED);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_LAPIS);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_LAPIS_BURIED);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, $$1 ? OrePlacements.ORE_COPPER_LARGE : OrePlacements.ORE_COPPER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CavePlacements.UNDERWATER_MAGMA);
    }

    public static void addExtraGold(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GOLD_EXTRA);
    }

    public static void addExtraEmeralds(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_EMERALD);
    }

    public static void addInfestedStone(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_INFESTED);
    }

    public static void addDefaultSoftDisks(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_SAND);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_CLAY);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_GRAVEL);
    }

    public static void addSwampClayDisk(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_CLAY);
    }

    public static void addMangroveSwampDisks(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_GRASS);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_CLAY);
    }

    public static void addMossyStoneBlock(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.FOREST_ROCK);
    }

    public static void addFerns(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_LARGE_FERN);
    }

    public static void addBushes(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_BUSH);
    }

    public static void addRareBerryBushes(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_BERRY_RARE);
    }

    public static void addCommonBerryBushes(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_BERRY_COMMON);
    }

    public static void addLightBambooVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BAMBOO_LIGHT);
    }

    public static void addBambooVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BAMBOO);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BAMBOO_VEGETATION);
    }

    public static void addTaigaTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_TAIGA);
    }

    public static void addGroveTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_GROVE);
    }

    public static void addWaterTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WATER);
    }

    public static void addBirchTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BIRCH);
    }

    public static void addOtherBirchTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BIRCH_AND_OAK_LEAF_LITTER);
    }

    public static void addTallBirchTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BIRCH_TALL);
    }

    public static void addBirchForestFlowers(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.WILDFLOWERS_BIRCH_FOREST);
    }

    public static void addSavannaTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SAVANNA);
    }

    public static void addShatteredSavannaTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WINDSWEPT_SAVANNA);
    }

    public static void addLushCavesVegetationFeatures(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_CEILING_VEGETATION);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CAVE_VINES);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_CLAY);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_VEGETATION);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.ROOTED_AZALEA_TREE);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.SPORE_BLOSSOM);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CLASSIC_VINES);
    }

    public static void addLushCavesSpecialOres(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
    }

    public static void addMountainTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WINDSWEPT_HILLS);
    }

    public static void addMountainForestTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WINDSWEPT_FOREST);
    }

    public static void addJungleTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_JUNGLE);
    }

    public static void addSparseJungleTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SPARSE_JUNGLE);
    }

    public static void addBadlandsTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BADLANDS);
    }

    public static void addSnowyTrees(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SNOWY);
    }

    public static void addJungleGrass(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_JUNGLE);
    }

    public static void addSavannaGrass(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_TALL_GRASS);
    }

    public static void addShatteredSavannaGrass(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_NORMAL);
    }

    public static void addSavannaExtraGrass(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_SAVANNA);
    }

    public static void addBadlandGrass(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_BADLANDS);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DRY_GRASS_BADLANDS);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH_BADLANDS);
    }

    public static void addForestFlowers(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FOREST_FLOWERS);
    }

    public static void addForestGrass(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_FOREST);
    }

    public static void addSwampVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SWAMP);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_SWAMP);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_NORMAL);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_SWAMP);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_SWAMP);
    }

    public static void addMangroveSwampVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_MANGROVE);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_NORMAL);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
    }

    public static void addMushroomFieldVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.MUSHROOM_ISLAND_VEGETATION);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_TAIGA);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_TAIGA);
    }

    public static void addPlainVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_PLAINS);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_PLAINS);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
    }

    public static void addDesertVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DRY_GRASS_DESERT);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH_2);
    }

    public static void addGiantTaigaVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_TAIGA);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_OLD_GROWTH);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_OLD_GROWTH);
    }

    public static void addDefaultFlowers(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_DEFAULT);
    }

    public static void addCherryGroveVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_CHERRY);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_CHERRY);
    }

    public static void addMeadowVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_MEADOW);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_MEADOW);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_MEADOW);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.WILDFLOWERS_MEADOW);
    }

    public static void addWarmFlowers(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_WARM);
    }

    public static void addDefaultGrass(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_BADLANDS);
    }

    public static void addTaigaGrass(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_TAIGA_2);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_TAIGA);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_TAIGA);
    }

    public static void addPlainGrass(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_TALL_GRASS_2);
    }

    public static void addDefaultMushrooms(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_NORMAL);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_NORMAL);
    }

    public static void addDefaultExtraVegetation(BiomeGenerationSettings.Builder $$0, boolean $$1) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
        if ($$1) {
            BiomeDefaultFeatures.addNearWaterVegetation($$0);
        }
    }

    public static void addNearWaterVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER);
    }

    public static void addLeafLitterPatch(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_LEAF_LITTER);
    }

    public static void addBadlandExtraVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE_BADLANDS);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_CACTUS_DECORATED);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER);
    }

    public static void addJungleMelons(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_MELON);
    }

    public static void addSparseJungleMelons(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_MELON_SPARSE);
    }

    public static void addJungleVines(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.VINES);
    }

    public static void addDesertExtraVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE_DESERT);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_CACTUS_DESERT);
    }

    public static void addSwampExtraVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE_SWAMP);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_FIREFLY_BUSH_SWAMP);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER_SWAMP);
    }

    public static void addMangroveSwampExtraVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER);
    }

    public static void addDesertExtraDecoration(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.DESERT_WELL);
    }

    public static void addFossilDecoration(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.FOSSIL_UPPER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.FOSSIL_LOWER);
    }

    public static void addColdOceanExtraVegetation(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.KELP_COLD);
    }

    public static void addLukeWarmKelp(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.KELP_WARM);
    }

    public static void addDefaultSprings(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_WATER);
        $$0.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_LAVA);
    }

    public static void addFrozenSprings(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_LAVA_FROZEN);
    }

    public static void addIcebergs(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.ICEBERG_PACKED);
        $$0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.ICEBERG_BLUE);
    }

    public static void addBlueIce(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.BLUE_ICE);
    }

    public static void addSurfaceFreezing(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, MiscOverworldPlacements.FREEZE_TOP_LAYER);
    }

    public static void addNetherDefaultOres(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_GRAVEL_NETHER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_BLACKSTONE);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_GOLD_NETHER);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_QUARTZ_NETHER);
        BiomeDefaultFeatures.addAncientDebris($$0);
    }

    public static void addAncientDebris(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_ANCIENT_DEBRIS_LARGE);
        $$0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_ANCIENT_DEBRIS_SMALL);
    }

    public static void addDefaultCrystalFormations(BiomeGenerationSettings.Builder $$0) {
        $$0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.AMETHYST_GEODE);
    }

    public static void farmAnimals(MobSpawnSettings.Builder $$0) {
        $$0.addSpawn(MobCategory.CREATURE, 12, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 4, 4));
        $$0.addSpawn(MobCategory.CREATURE, 10, new MobSpawnSettings.SpawnerData(EntityType.PIG, 4, 4));
        $$0.addSpawn(MobCategory.CREATURE, 10, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 4, 4));
        $$0.addSpawn(MobCategory.CREATURE, 8, new MobSpawnSettings.SpawnerData(EntityType.COW, 4, 4));
    }

    public static void caveSpawns(MobSpawnSettings.Builder $$0) {
        $$0.addSpawn(MobCategory.AMBIENT, 10, new MobSpawnSettings.SpawnerData(EntityType.BAT, 8, 8));
        $$0.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, 10, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 4, 6));
    }

    public static void commonSpawns(MobSpawnSettings.Builder $$0) {
        BiomeDefaultFeatures.commonSpawns($$0, 100);
    }

    public static void commonSpawns(MobSpawnSettings.Builder $$0, int $$1) {
        BiomeDefaultFeatures.caveSpawns($$0);
        BiomeDefaultFeatures.monsters($$0, 95, 5, 0, $$1, false);
    }

    public static void commonSpawnWithZombieHorse(MobSpawnSettings.Builder $$0) {
        BiomeDefaultFeatures.caveSpawns($$0);
        BiomeDefaultFeatures.monsters($$0, 90, 5, 5, 100, false);
    }

    public static void swampSpawns(MobSpawnSettings.Builder $$0, int $$1) {
        BiomeDefaultFeatures.commonSpawns($$0, $$1);
        $$0.addSpawn(MobCategory.MONSTER, 1, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1));
        $$0.addSpawn(MobCategory.MONSTER, 30, new MobSpawnSettings.SpawnerData(EntityType.BOGGED, 4, 4));
        $$0.addSpawn(MobCategory.CREATURE, 10, new MobSpawnSettings.SpawnerData(EntityType.FROG, 2, 5));
    }

    public static void oceanSpawns(MobSpawnSettings.Builder $$0, int $$1, int $$2, int $$3) {
        $$0.addSpawn(MobCategory.WATER_CREATURE, $$1, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, $$2));
        $$0.addSpawn(MobCategory.WATER_AMBIENT, $$3, new MobSpawnSettings.SpawnerData(EntityType.COD, 3, 6));
        BiomeDefaultFeatures.commonSpawns($$0);
        $$0.addSpawn(MobCategory.MONSTER, 5, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 1, 1));
    }

    public static void warmOceanSpawns(MobSpawnSettings.Builder $$0, int $$1, int $$2) {
        $$0.addSpawn(MobCategory.WATER_CREATURE, $$1, new MobSpawnSettings.SpawnerData(EntityType.SQUID, $$2, 4));
        $$0.addSpawn(MobCategory.WATER_AMBIENT, 25, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 8, 8));
        $$0.addSpawn(MobCategory.WATER_CREATURE, 2, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 2));
        $$0.addSpawn(MobCategory.MONSTER, 5, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 1, 1));
        BiomeDefaultFeatures.commonSpawns($$0);
    }

    public static void plainsSpawns(MobSpawnSettings.Builder $$0) {
        BiomeDefaultFeatures.farmAnimals($$0);
        $$0.addSpawn(MobCategory.CREATURE, 5, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 2, 6));
        $$0.addSpawn(MobCategory.CREATURE, 1, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 3));
        BiomeDefaultFeatures.commonSpawnWithZombieHorse($$0);
    }

    public static void snowySpawns(MobSpawnSettings.Builder $$0, boolean $$1) {
        $$0.addSpawn(MobCategory.CREATURE, 10, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 3));
        $$0.addSpawn(MobCategory.CREATURE, 1, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 2));
        BiomeDefaultFeatures.caveSpawns($$0);
        BiomeDefaultFeatures.monsters($$0, $$1 ? 90 : 95, 5, $$1 ? 5 : 0, 20, false);
        $$0.addSpawn(MobCategory.MONSTER, 80, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 4, 4));
    }

    public static void desertSpawns(MobSpawnSettings.Builder $$0) {
        $$0.addSpawn(MobCategory.CREATURE, 12, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 3));
        $$0.addSpawn(MobCategory.CREATURE, 1, new MobSpawnSettings.SpawnerData(EntityType.CAMEL, 1, 1));
        BiomeDefaultFeatures.caveSpawns($$0);
        BiomeDefaultFeatures.monsters($$0, 19, 1, 0, 50, false);
        $$0.addSpawn(MobCategory.MONSTER, 80, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 4, 4));
        $$0.addSpawn(MobCategory.MONSTER, 50, new MobSpawnSettings.SpawnerData(EntityType.PARCHED, 4, 4));
    }

    public static void dripstoneCavesSpawns(MobSpawnSettings.Builder $$0) {
        BiomeDefaultFeatures.caveSpawns($$0);
        int $$1 = 95;
        BiomeDefaultFeatures.monsters($$0, 95, 5, 0, 100, false);
        $$0.addSpawn(MobCategory.MONSTER, 95, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 4, 4));
    }

    public static void monsters(MobSpawnSettings.Builder $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
        $$0.addSpawn(MobCategory.MONSTER, 100, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 4, 4));
        $$0.addSpawn(MobCategory.MONSTER, $$1, new MobSpawnSettings.SpawnerData($$5 ? EntityType.DROWNED : EntityType.ZOMBIE, 4, 4));
        $$0.addSpawn(MobCategory.MONSTER, $$2, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, 1, 1));
        if ($$3 > 0) {
            $$0.addSpawn(MobCategory.MONSTER, $$3, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_HORSE, 1, 1));
        }
        $$0.addSpawn(MobCategory.MONSTER, $$4, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 4, 4));
        $$0.addSpawn(MobCategory.MONSTER, 100, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 4, 4));
        $$0.addSpawn(MobCategory.MONSTER, 100, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 4, 4));
        $$0.addSpawn(MobCategory.MONSTER, 10, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4));
        $$0.addSpawn(MobCategory.MONSTER, 5, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1));
    }

    public static void mooshroomSpawns(MobSpawnSettings.Builder $$0) {
        $$0.addSpawn(MobCategory.CREATURE, 8, new MobSpawnSettings.SpawnerData(EntityType.MOOSHROOM, 4, 8));
        BiomeDefaultFeatures.caveSpawns($$0);
    }

    public static void baseJungleSpawns(MobSpawnSettings.Builder $$0) {
        BiomeDefaultFeatures.farmAnimals($$0);
        $$0.addSpawn(MobCategory.CREATURE, 10, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 4, 4));
        BiomeDefaultFeatures.commonSpawns($$0);
    }

    public static void endSpawns(MobSpawnSettings.Builder $$0) {
        $$0.addSpawn(MobCategory.MONSTER, 10, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 4, 4));
    }
}

