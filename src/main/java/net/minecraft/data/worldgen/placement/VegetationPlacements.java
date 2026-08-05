package net.minecraft.data.worldgen.placement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.NoiseThresholdCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

public class VegetationPlacements {
   public static final ResourceKey<PlacedFeature> BAMBOO_LIGHT = PlacementUtils.createKey("bamboo_light");
   public static final ResourceKey<PlacedFeature> BAMBOO = PlacementUtils.createKey("bamboo");
   public static final ResourceKey<PlacedFeature> VINES = PlacementUtils.createKey("vines");
   public static final ResourceKey<PlacedFeature> PATCH_SUNFLOWER = PlacementUtils.createKey("patch_sunflower");
   public static final ResourceKey<PlacedFeature> PATCH_PUMPKIN = PlacementUtils.createKey("patch_pumpkin");
   public static final ResourceKey<PlacedFeature> PATCH_GRASS_PLAIN = PlacementUtils.createKey("patch_grass_plain");
   public static final ResourceKey<PlacedFeature> PATCH_GRASS_MEADOW = PlacementUtils.createKey("patch_grass_meadow");
   public static final ResourceKey<PlacedFeature> PATCH_GRASS_FOREST = PlacementUtils.createKey("patch_grass_forest");
   public static final ResourceKey<PlacedFeature> PATCH_GRASS_BADLANDS = PlacementUtils.createKey("patch_grass_badlands");
   public static final ResourceKey<PlacedFeature> PATCH_GRASS_SAVANNA = PlacementUtils.createKey("patch_grass_savanna");
   public static final ResourceKey<PlacedFeature> PATCH_GRASS_NORMAL = PlacementUtils.createKey("patch_grass_normal");
   public static final ResourceKey<PlacedFeature> PATCH_GRASS_TAIGA_2 = PlacementUtils.createKey("patch_grass_taiga_2");
   public static final ResourceKey<PlacedFeature> PATCH_GRASS_TAIGA = PlacementUtils.createKey("patch_grass_taiga");
   public static final ResourceKey<PlacedFeature> PATCH_GRASS_JUNGLE = PlacementUtils.createKey("patch_grass_jungle");
   public static final ResourceKey<PlacedFeature> GRASS_BONEMEAL = PlacementUtils.createKey("grass_bonemeal");
   public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH_2 = PlacementUtils.createKey("patch_dead_bush_2");
   public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH = PlacementUtils.createKey("patch_dead_bush");
   public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH_BADLANDS = PlacementUtils.createKey("patch_dead_bush_badlands");
   public static final ResourceKey<PlacedFeature> PATCH_DRY_GRASS_BADLANDS = PlacementUtils.createKey("patch_dry_grass_badlands");
   public static final ResourceKey<PlacedFeature> PATCH_DRY_GRASS_DESERT = PlacementUtils.createKey("patch_dry_grass_desert");
   public static final ResourceKey<PlacedFeature> PATCH_MELON = PlacementUtils.createKey("patch_melon");
   public static final ResourceKey<PlacedFeature> PATCH_MELON_SPARSE = PlacementUtils.createKey("patch_melon_sparse");
   public static final ResourceKey<PlacedFeature> PATCH_BERRY_COMMON = PlacementUtils.createKey("patch_berry_common");
   public static final ResourceKey<PlacedFeature> PATCH_BERRY_RARE = PlacementUtils.createKey("patch_berry_rare");
   public static final ResourceKey<PlacedFeature> PATCH_WATERLILY = PlacementUtils.createKey("patch_waterlily");
   public static final ResourceKey<PlacedFeature> PATCH_TALL_GRASS_2 = PlacementUtils.createKey("patch_tall_grass_2");
   public static final ResourceKey<PlacedFeature> PATCH_TALL_GRASS = PlacementUtils.createKey("patch_tall_grass");
   public static final ResourceKey<PlacedFeature> PATCH_LARGE_FERN = PlacementUtils.createKey("patch_large_fern");
   public static final ResourceKey<PlacedFeature> PATCH_BUSH = PlacementUtils.createKey("patch_bush");
   public static final ResourceKey<PlacedFeature> PATCH_LEAF_LITTER = PlacementUtils.createKey("patch_leaf_litter");
   public static final ResourceKey<PlacedFeature> PATCH_CACTUS_DESERT = PlacementUtils.createKey("patch_cactus_desert");
   public static final ResourceKey<PlacedFeature> PATCH_CACTUS_DECORATED = PlacementUtils.createKey("patch_cactus_decorated");
   public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_SWAMP = PlacementUtils.createKey("patch_sugar_cane_swamp");
   public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_DESERT = PlacementUtils.createKey("patch_sugar_cane_desert");
   public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_BADLANDS = PlacementUtils.createKey("patch_sugar_cane_badlands");
   public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE = PlacementUtils.createKey("patch_sugar_cane");
   public static final ResourceKey<PlacedFeature> PATCH_FIREFLY_BUSH_SWAMP = PlacementUtils.createKey("patch_firefly_bush_swamp");
   public static final ResourceKey<PlacedFeature> PATCH_FIREFLY_BUSH_NEAR_WATER_SWAMP = PlacementUtils.createKey("patch_firefly_bush_near_water_swamp");
   public static final ResourceKey<PlacedFeature> PATCH_FIREFLY_BUSH_NEAR_WATER = PlacementUtils.createKey("patch_firefly_bush_near_water");
   public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_NETHER = PlacementUtils.createKey("brown_mushroom_nether");
   public static final ResourceKey<PlacedFeature> RED_MUSHROOM_NETHER = PlacementUtils.createKey("red_mushroom_nether");
   public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_NORMAL = PlacementUtils.createKey("brown_mushroom_normal");
   public static final ResourceKey<PlacedFeature> RED_MUSHROOM_NORMAL = PlacementUtils.createKey("red_mushroom_normal");
   public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_TAIGA = PlacementUtils.createKey("brown_mushroom_taiga");
   public static final ResourceKey<PlacedFeature> RED_MUSHROOM_TAIGA = PlacementUtils.createKey("red_mushroom_taiga");
   public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_OLD_GROWTH = PlacementUtils.createKey("brown_mushroom_old_growth");
   public static final ResourceKey<PlacedFeature> RED_MUSHROOM_OLD_GROWTH = PlacementUtils.createKey("red_mushroom_old_growth");
   public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_SWAMP = PlacementUtils.createKey("brown_mushroom_swamp");
   public static final ResourceKey<PlacedFeature> RED_MUSHROOM_SWAMP = PlacementUtils.createKey("red_mushroom_swamp");
   public static final ResourceKey<PlacedFeature> FLOWER_WARM = PlacementUtils.createKey("flower_warm");
   public static final ResourceKey<PlacedFeature> FLOWER_DEFAULT = PlacementUtils.createKey("flower_default");
   public static final ResourceKey<PlacedFeature> FLOWER_FLOWER_FOREST = PlacementUtils.createKey("flower_flower_forest");
   public static final ResourceKey<PlacedFeature> FLOWER_SWAMP = PlacementUtils.createKey("flower_swamp");
   public static final ResourceKey<PlacedFeature> FLOWER_PLAINS = PlacementUtils.createKey("flower_plains");
   public static final ResourceKey<PlacedFeature> FLOWER_MEADOW = PlacementUtils.createKey("flower_meadow");
   public static final ResourceKey<PlacedFeature> FLOWER_CHERRY = PlacementUtils.createKey("flower_cherry");
   public static final ResourceKey<PlacedFeature> FLOWER_PALE_GARDEN = PlacementUtils.createKey("flower_pale_garden");
   public static final ResourceKey<PlacedFeature> WILDFLOWERS_BIRCH_FOREST = PlacementUtils.createKey("wildflowers_birch_forest");
   public static final ResourceKey<PlacedFeature> WILDFLOWERS_MEADOW = PlacementUtils.createKey("wildflowers_meadow");
   public static final ResourceKey<PlacedFeature> TREES_PLAINS = PlacementUtils.createKey("trees_plains");
   public static final ResourceKey<PlacedFeature> DARK_FOREST_VEGETATION = PlacementUtils.createKey("dark_forest_vegetation");
   public static final ResourceKey<PlacedFeature> PALE_GARDEN_VEGETATION = PlacementUtils.createKey("pale_garden_vegetation");
   public static final ResourceKey<PlacedFeature> FLOWER_FOREST_FLOWERS = PlacementUtils.createKey("flower_forest_flowers");
   public static final ResourceKey<PlacedFeature> FOREST_FLOWERS = PlacementUtils.createKey("forest_flowers");
   public static final ResourceKey<PlacedFeature> PALE_GARDEN_FLOWERS = PlacementUtils.createKey("pale_garden_flowers");
   public static final ResourceKey<PlacedFeature> PALE_MOSS_PATCH = PlacementUtils.createKey("pale_moss_patch");
   public static final ResourceKey<PlacedFeature> TREES_FLOWER_FOREST = PlacementUtils.createKey("trees_flower_forest");
   public static final ResourceKey<PlacedFeature> TREES_MEADOW = PlacementUtils.createKey("trees_meadow");
   public static final ResourceKey<PlacedFeature> TREES_CHERRY = PlacementUtils.createKey("trees_cherry");
   public static final ResourceKey<PlacedFeature> TREES_TAIGA = PlacementUtils.createKey("trees_taiga");
   public static final ResourceKey<PlacedFeature> TREES_GROVE = PlacementUtils.createKey("trees_grove");
   public static final ResourceKey<PlacedFeature> TREES_BADLANDS = PlacementUtils.createKey("trees_badlands");
   public static final ResourceKey<PlacedFeature> TREES_SNOWY = PlacementUtils.createKey("trees_snowy");
   public static final ResourceKey<PlacedFeature> TREES_SWAMP = PlacementUtils.createKey("trees_swamp");
   public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_SAVANNA = PlacementUtils.createKey("trees_windswept_savanna");
   public static final ResourceKey<PlacedFeature> TREES_SAVANNA = PlacementUtils.createKey("trees_savanna");
   public static final ResourceKey<PlacedFeature> BIRCH_TALL = PlacementUtils.createKey("birch_tall");
   public static final ResourceKey<PlacedFeature> TREES_BIRCH = PlacementUtils.createKey("trees_birch");
   public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_FOREST = PlacementUtils.createKey("trees_windswept_forest");
   public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_HILLS = PlacementUtils.createKey("trees_windswept_hills");
   public static final ResourceKey<PlacedFeature> TREES_WATER = PlacementUtils.createKey("trees_water");
   public static final ResourceKey<PlacedFeature> TREES_BIRCH_AND_OAK_LEAF_LITTER = PlacementUtils.createKey("trees_birch_and_oak_leaf_litter");
   public static final ResourceKey<PlacedFeature> TREES_SPARSE_JUNGLE = PlacementUtils.createKey("trees_sparse_jungle");
   public static final ResourceKey<PlacedFeature> TREES_OLD_GROWTH_SPRUCE_TAIGA = PlacementUtils.createKey("trees_old_growth_spruce_taiga");
   public static final ResourceKey<PlacedFeature> TREES_OLD_GROWTH_PINE_TAIGA = PlacementUtils.createKey("trees_old_growth_pine_taiga");
   public static final ResourceKey<PlacedFeature> TREES_JUNGLE = PlacementUtils.createKey("trees_jungle");
   public static final ResourceKey<PlacedFeature> BAMBOO_VEGETATION = PlacementUtils.createKey("bamboo_vegetation");
   public static final ResourceKey<PlacedFeature> MUSHROOM_ISLAND_VEGETATION = PlacementUtils.createKey("mushroom_island_vegetation");
   public static final ResourceKey<PlacedFeature> TREES_MANGROVE = PlacementUtils.createKey("trees_mangrove");
   private static final PlacementModifier TREE_THRESHOLD = SurfaceWaterDepthFilter.forMaxDepth(0);

   public static List<PlacementModifier> worldSurfaceSquaredWithCount(int $$0) {
      return List.of(CountPlacement.of($$0), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
   }

   private static List<PlacementModifier> getMushroomPlacement(int $$0, PlacementModifier $$1) {
      Builder<PlacementModifier> $$2 = ImmutableList.builder();
      if ($$1 != null) {
         $$2.add($$1);
      }

      if ($$0 != 0) {
         $$2.add(RarityFilter.onAverageOnceEvery($$0));
      }

      $$2.add(InSquarePlacement.spread());
      $$2.add(PlacementUtils.HEIGHTMAP);
      $$2.add(BiomeFilter.biome());
      return $$2.build();
   }

   private static Builder<PlacementModifier> treePlacementBase(PlacementModifier $$0) {
      return ImmutableList.builder()
         .add($$0)
         .add(InSquarePlacement.spread())
         .add(TREE_THRESHOLD)
         .add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR)
         .add(BiomeFilter.biome());
   }

   public static List<PlacementModifier> treePlacement(PlacementModifier $$0) {
      return treePlacementBase($$0).build();
   }

   public static List<PlacementModifier> treePlacement(PlacementModifier $$0, Block $$1) {
      return treePlacementBase($$0).add(BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive($$1.defaultBlockState(), BlockPos.ZERO))).build();
   }

   public static void bootstrap(BootstrapContext<PlacedFeature> $$0) {
      HolderGetter<ConfiguredFeature<?, ?>> $$1 = $$0.lookup(Registries.CONFIGURED_FEATURE);
      Holder<ConfiguredFeature<?, ?>> $$2 = $$1.getOrThrow(VegetationFeatures.BAMBOO_NO_PODZOL);
      Holder<ConfiguredFeature<?, ?>> $$3 = $$1.getOrThrow(VegetationFeatures.BAMBOO_SOME_PODZOL);
      Holder<ConfiguredFeature<?, ?>> $$4 = $$1.getOrThrow(VegetationFeatures.VINES);
      Holder<ConfiguredFeature<?, ?>> $$5 = $$1.getOrThrow(VegetationFeatures.PATCH_SUNFLOWER);
      Holder<ConfiguredFeature<?, ?>> $$6 = $$1.getOrThrow(VegetationFeatures.PATCH_PUMPKIN);
      Holder<ConfiguredFeature<?, ?>> $$7 = $$1.getOrThrow(VegetationFeatures.PATCH_GRASS);
      Holder<ConfiguredFeature<?, ?>> $$8 = $$1.getOrThrow(VegetationFeatures.PATCH_GRASS_MEADOW);
      Holder<ConfiguredFeature<?, ?>> $$9 = $$1.getOrThrow(VegetationFeatures.PATCH_LEAF_LITTER);
      Holder<ConfiguredFeature<?, ?>> $$10 = $$1.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
      Holder<ConfiguredFeature<?, ?>> $$11 = $$1.getOrThrow(VegetationFeatures.PATCH_GRASS_JUNGLE);
      Holder<ConfiguredFeature<?, ?>> $$12 = $$1.getOrThrow(VegetationFeatures.SINGLE_PIECE_OF_GRASS);
      Holder<ConfiguredFeature<?, ?>> $$13 = $$1.getOrThrow(VegetationFeatures.PATCH_DEAD_BUSH);
      Holder<ConfiguredFeature<?, ?>> $$14 = $$1.getOrThrow(VegetationFeatures.PATCH_DRY_GRASS);
      Holder<ConfiguredFeature<?, ?>> $$15 = $$1.getOrThrow(VegetationFeatures.PATCH_FIREFLY_BUSH);
      Holder<ConfiguredFeature<?, ?>> $$16 = $$1.getOrThrow(VegetationFeatures.PATCH_MELON);
      Holder<ConfiguredFeature<?, ?>> $$17 = $$1.getOrThrow(VegetationFeatures.PATCH_BERRY_BUSH);
      Holder<ConfiguredFeature<?, ?>> $$18 = $$1.getOrThrow(VegetationFeatures.PATCH_WATERLILY);
      Holder<ConfiguredFeature<?, ?>> $$19 = $$1.getOrThrow(VegetationFeatures.PATCH_TALL_GRASS);
      Holder<ConfiguredFeature<?, ?>> $$20 = $$1.getOrThrow(VegetationFeatures.PATCH_LARGE_FERN);
      Holder<ConfiguredFeature<?, ?>> $$21 = $$1.getOrThrow(VegetationFeatures.PATCH_BUSH);
      Holder<ConfiguredFeature<?, ?>> $$22 = $$1.getOrThrow(VegetationFeatures.PATCH_CACTUS);
      Holder<ConfiguredFeature<?, ?>> $$23 = $$1.getOrThrow(VegetationFeatures.PATCH_SUGAR_CANE);
      Holder<ConfiguredFeature<?, ?>> $$24 = $$1.getOrThrow(VegetationFeatures.PATCH_BROWN_MUSHROOM);
      Holder<ConfiguredFeature<?, ?>> $$25 = $$1.getOrThrow(VegetationFeatures.PATCH_RED_MUSHROOM);
      Holder<ConfiguredFeature<?, ?>> $$26 = $$1.getOrThrow(VegetationFeatures.FLOWER_DEFAULT);
      Holder<ConfiguredFeature<?, ?>> $$27 = $$1.getOrThrow(VegetationFeatures.FLOWER_FLOWER_FOREST);
      Holder<ConfiguredFeature<?, ?>> $$28 = $$1.getOrThrow(VegetationFeatures.FLOWER_SWAMP);
      Holder<ConfiguredFeature<?, ?>> $$29 = $$1.getOrThrow(VegetationFeatures.FLOWER_PLAIN);
      Holder<ConfiguredFeature<?, ?>> $$30 = $$1.getOrThrow(VegetationFeatures.FLOWER_MEADOW);
      Holder<ConfiguredFeature<?, ?>> $$31 = $$1.getOrThrow(VegetationFeatures.FLOWER_CHERRY);
      Holder<ConfiguredFeature<?, ?>> $$32 = $$1.getOrThrow(VegetationFeatures.FLOWER_PALE_GARDEN);
      Holder<ConfiguredFeature<?, ?>> $$33 = $$1.getOrThrow(VegetationFeatures.WILDFLOWERS_BIRCH_FOREST);
      Holder<ConfiguredFeature<?, ?>> $$34 = $$1.getOrThrow(VegetationFeatures.WILDFLOWERS_MEADOW);
      Holder<ConfiguredFeature<?, ?>> $$35 = $$1.getOrThrow(VegetationFeatures.TREES_PLAINS);
      Holder<ConfiguredFeature<?, ?>> $$36 = $$1.getOrThrow(VegetationFeatures.DARK_FOREST_VEGETATION);
      Holder<ConfiguredFeature<?, ?>> $$37 = $$1.getOrThrow(VegetationFeatures.PALE_GARDEN_VEGETATION);
      Holder<ConfiguredFeature<?, ?>> $$38 = $$1.getOrThrow(VegetationFeatures.FOREST_FLOWERS);
      Holder<ConfiguredFeature<?, ?>> $$39 = $$1.getOrThrow(VegetationFeatures.PALE_FOREST_FLOWERS);
      Holder<ConfiguredFeature<?, ?>> $$40 = $$1.getOrThrow(VegetationFeatures.PALE_MOSS_PATCH);
      Holder<ConfiguredFeature<?, ?>> $$41 = $$1.getOrThrow(VegetationFeatures.TREES_FLOWER_FOREST);
      Holder<ConfiguredFeature<?, ?>> $$42 = $$1.getOrThrow(VegetationFeatures.MEADOW_TREES);
      Holder<ConfiguredFeature<?, ?>> $$43 = $$1.getOrThrow(VegetationFeatures.TREES_TAIGA);
      Holder<ConfiguredFeature<?, ?>> $$44 = $$1.getOrThrow(VegetationFeatures.TREES_BADLANDS);
      Holder<ConfiguredFeature<?, ?>> $$45 = $$1.getOrThrow(VegetationFeatures.TREES_GROVE);
      Holder<ConfiguredFeature<?, ?>> $$46 = $$1.getOrThrow(VegetationFeatures.TREES_SNOWY);
      Holder<ConfiguredFeature<?, ?>> $$47 = $$1.getOrThrow(TreeFeatures.CHERRY_BEES_005);
      Holder<ConfiguredFeature<?, ?>> $$48 = $$1.getOrThrow(TreeFeatures.SWAMP_OAK);
      Holder<ConfiguredFeature<?, ?>> $$49 = $$1.getOrThrow(VegetationFeatures.TREES_SAVANNA);
      Holder<ConfiguredFeature<?, ?>> $$50 = $$1.getOrThrow(VegetationFeatures.BIRCH_TALL);
      Holder<ConfiguredFeature<?, ?>> $$51 = $$1.getOrThrow(VegetationFeatures.TREES_BIRCH);
      Holder<ConfiguredFeature<?, ?>> $$52 = $$1.getOrThrow(VegetationFeatures.TREES_WINDSWEPT_HILLS);
      Holder<ConfiguredFeature<?, ?>> $$53 = $$1.getOrThrow(VegetationFeatures.TREES_WATER);
      Holder<ConfiguredFeature<?, ?>> $$54 = $$1.getOrThrow(VegetationFeatures.TREES_BIRCH_AND_OAK_LEAF_LITTER);
      Holder<ConfiguredFeature<?, ?>> $$55 = $$1.getOrThrow(VegetationFeatures.TREES_SPARSE_JUNGLE);
      Holder<ConfiguredFeature<?, ?>> $$56 = $$1.getOrThrow(VegetationFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA);
      Holder<ConfiguredFeature<?, ?>> $$57 = $$1.getOrThrow(VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA);
      Holder<ConfiguredFeature<?, ?>> $$58 = $$1.getOrThrow(VegetationFeatures.TREES_JUNGLE);
      Holder<ConfiguredFeature<?, ?>> $$59 = $$1.getOrThrow(VegetationFeatures.BAMBOO_VEGETATION);
      Holder<ConfiguredFeature<?, ?>> $$60 = $$1.getOrThrow(VegetationFeatures.MUSHROOM_ISLAND_VEGETATION);
      Holder<ConfiguredFeature<?, ?>> $$61 = $$1.getOrThrow(VegetationFeatures.MANGROVE_VEGETATION);
      PlacementUtils.register(
         $$0, BAMBOO_LIGHT, $$2, RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, BAMBOO, $$3, NoiseBasedCountPlacement.of(160, 80.0, 0.3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         VINES,
         $$4,
         CountPlacement.of(127),
         InSquarePlacement.spread(),
         HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(100)),
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_SUNFLOWER, $$5, RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_PUMPKIN, $$6, RarityFilter.onAverageOnceEvery(300), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         PATCH_GRASS_PLAIN,
         $$7,
         NoiseThresholdCountPlacement.of(-0.8, 5, 10),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         PATCH_GRASS_MEADOW,
         $$8,
         NoiseThresholdCountPlacement.of(-0.8, 5, 10),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
         BiomeFilter.biome()
      );
      PlacementUtils.register($$0, PATCH_GRASS_FOREST, $$7, worldSurfaceSquaredWithCount(2));
      PlacementUtils.register($$0, PATCH_LEAF_LITTER, $$9, worldSurfaceSquaredWithCount(2));
      PlacementUtils.register($$0, PATCH_GRASS_BADLANDS, $$7, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
      PlacementUtils.register($$0, PATCH_GRASS_SAVANNA, $$7, worldSurfaceSquaredWithCount(20));
      PlacementUtils.register($$0, PATCH_GRASS_NORMAL, $$7, worldSurfaceSquaredWithCount(5));
      PlacementUtils.register($$0, PATCH_GRASS_TAIGA_2, $$10, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
      PlacementUtils.register($$0, PATCH_GRASS_TAIGA, $$10, worldSurfaceSquaredWithCount(7));
      PlacementUtils.register($$0, PATCH_GRASS_JUNGLE, $$11, worldSurfaceSquaredWithCount(25));
      PlacementUtils.register($$0, GRASS_BONEMEAL, $$12, PlacementUtils.isEmpty());
      PlacementUtils.register($$0, PATCH_DEAD_BUSH_2, $$13, worldSurfaceSquaredWithCount(2));
      PlacementUtils.register($$0, PATCH_DEAD_BUSH, $$13, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
      PlacementUtils.register($$0, PATCH_DEAD_BUSH_BADLANDS, $$13, worldSurfaceSquaredWithCount(20));
      PlacementUtils.register(
         $$0, PATCH_DRY_GRASS_BADLANDS, $$14, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_DRY_GRASS_DESERT, $$14, RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_MELON, $$16, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_MELON_SPARSE, $$16, RarityFilter.onAverageOnceEvery(64), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         PATCH_BERRY_COMMON,
         $$17,
         RarityFilter.onAverageOnceEvery(32),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         PATCH_BERRY_RARE,
         $$17,
         RarityFilter.onAverageOnceEvery(384),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
         BiomeFilter.biome()
      );
      PlacementUtils.register($$0, PATCH_WATERLILY, $$18, worldSurfaceSquaredWithCount(4));
      PlacementUtils.register(
         $$0,
         PATCH_TALL_GRASS_2,
         $$19,
         NoiseThresholdCountPlacement.of(-0.8, 0, 7),
         RarityFilter.onAverageOnceEvery(32),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP,
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_TALL_GRASS, $$19, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_LARGE_FERN, $$20, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_BUSH, $$21, RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_CACTUS_DESERT, $$22, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_CACTUS_DECORATED, $$22, RarityFilter.onAverageOnceEvery(13), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_SUGAR_CANE_SWAMP, $$23, RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register($$0, PATCH_SUGAR_CANE_DESERT, $$23, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
      PlacementUtils.register(
         $$0, PATCH_SUGAR_CANE_BADLANDS, $$23, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PATCH_SUGAR_CANE, $$23, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         PATCH_FIREFLY_BUSH_NEAR_WATER,
         $$15,
         CountPlacement.of(2),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP_NO_LEAVES,
         BiomeFilter.biome(),
         VegetationFeatures.nearWaterPredicate(Blocks.FIREFLY_BUSH)
      );
      PlacementUtils.register(
         $$0,
         PATCH_FIREFLY_BUSH_NEAR_WATER_SWAMP,
         $$15,
         CountPlacement.of(3),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP,
         BiomeFilter.biome(),
         VegetationFeatures.nearWaterPredicate(Blocks.FIREFLY_BUSH)
      );
      PlacementUtils.register(
         $$0, PATCH_FIREFLY_BUSH_SWAMP, $$15, RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, BROWN_MUSHROOM_NETHER, $$24, RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, RED_MUSHROOM_NETHER, $$25, RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
      );
      PlacementUtils.register($$0, BROWN_MUSHROOM_NORMAL, $$24, getMushroomPlacement(256, null));
      PlacementUtils.register($$0, RED_MUSHROOM_NORMAL, $$25, getMushroomPlacement(512, null));
      PlacementUtils.register($$0, BROWN_MUSHROOM_TAIGA, $$24, getMushroomPlacement(4, null));
      PlacementUtils.register($$0, RED_MUSHROOM_TAIGA, $$25, getMushroomPlacement(256, null));
      PlacementUtils.register($$0, BROWN_MUSHROOM_OLD_GROWTH, $$24, getMushroomPlacement(4, CountPlacement.of(3)));
      PlacementUtils.register($$0, RED_MUSHROOM_OLD_GROWTH, $$25, getMushroomPlacement(171, null));
      PlacementUtils.register($$0, BROWN_MUSHROOM_SWAMP, $$24, getMushroomPlacement(0, CountPlacement.of(2)));
      PlacementUtils.register($$0, RED_MUSHROOM_SWAMP, $$25, getMushroomPlacement(64, null));
      PlacementUtils.register(
         $$0, FLOWER_WARM, $$26, RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, FLOWER_DEFAULT, $$26, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         FLOWER_FLOWER_FOREST,
         $$27,
         CountPlacement.of(3),
         RarityFilter.onAverageOnceEvery(2),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP,
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, FLOWER_SWAMP, $$28, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         FLOWER_PLAINS,
         $$29,
         NoiseThresholdCountPlacement.of(-0.8, 15, 4),
         RarityFilter.onAverageOnceEvery(32),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP,
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, FLOWER_CHERRY, $$31, NoiseThresholdCountPlacement.of(-0.8, 5, 10), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register($$0, FLOWER_MEADOW, $$30, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
      PlacementUtils.register(
         $$0, FLOWER_PALE_GARDEN, $$32, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         WILDFLOWERS_BIRCH_FOREST,
         $$33,
         CountPlacement.of(3),
         RarityFilter.onAverageOnceEvery(2),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP,
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, WILDFLOWERS_MEADOW, $$34, NoiseThresholdCountPlacement.of(-0.8, 5, 10), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementModifier $$62 = SurfaceWaterDepthFilter.forMaxDepth(0);
      PlacementUtils.register(
         $$0,
         TREES_PLAINS,
         $$35,
         PlacementUtils.countExtra(0, 0.05F, 1),
         InSquarePlacement.spread(),
         $$62,
         PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
         BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO)),
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, DARK_FOREST_VEGETATION, $$36, CountPlacement.of(16), InSquarePlacement.spread(), $$62, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PALE_GARDEN_VEGETATION, $$37, CountPlacement.of(16), InSquarePlacement.spread(), $$62, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         FLOWER_FOREST_FLOWERS,
         $$38,
         RarityFilter.onAverageOnceEvery(7),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP,
         CountPlacement.of(ClampedInt.of(UniformInt.of(-1, 3), 0, 3)),
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         FOREST_FLOWERS,
         $$38,
         RarityFilter.onAverageOnceEvery(7),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP,
         CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)),
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         PALE_GARDEN_FLOWERS,
         $$39,
         RarityFilter.onAverageOnceEvery(8),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP_NO_LEAVES,
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, PALE_MOSS_PATCH, $$40, CountPlacement.of(1), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_NO_LEAVES, BiomeFilter.biome()
      );
      PlacementUtils.register($$0, TREES_FLOWER_FOREST, $$41, treePlacement(PlacementUtils.countExtra(6, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_MEADOW, $$42, treePlacement(RarityFilter.onAverageOnceEvery(100)));
      PlacementUtils.register($$0, TREES_CHERRY, $$47, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1), Blocks.CHERRY_SAPLING));
      PlacementUtils.register($$0, TREES_TAIGA, $$43, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_GROVE, $$45, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_BADLANDS, $$44, treePlacement(PlacementUtils.countExtra(5, 0.1F, 1), Blocks.OAK_SAPLING));
      PlacementUtils.register($$0, TREES_SNOWY, $$46, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1), Blocks.SPRUCE_SAPLING));
      PlacementUtils.register(
         $$0,
         TREES_SWAMP,
         $$48,
         PlacementUtils.countExtra(2, 0.1F, 1),
         InSquarePlacement.spread(),
         SurfaceWaterDepthFilter.forMaxDepth(2),
         PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
         BiomeFilter.biome(),
         BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO))
      );
      PlacementUtils.register($$0, TREES_WINDSWEPT_SAVANNA, $$49, treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_SAVANNA, $$49, treePlacement(PlacementUtils.countExtra(1, 0.1F, 1)));
      PlacementUtils.register($$0, BIRCH_TALL, $$50, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_BIRCH, $$51, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1), Blocks.BIRCH_SAPLING));
      PlacementUtils.register($$0, TREES_WINDSWEPT_FOREST, $$52, treePlacement(PlacementUtils.countExtra(3, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_WINDSWEPT_HILLS, $$52, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_WATER, $$53, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_BIRCH_AND_OAK_LEAF_LITTER, $$54, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_SPARSE_JUNGLE, $$55, treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_OLD_GROWTH_SPRUCE_TAIGA, $$56, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_OLD_GROWTH_PINE_TAIGA, $$57, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
      PlacementUtils.register($$0, TREES_JUNGLE, $$58, treePlacement(PlacementUtils.countExtra(50, 0.1F, 1)));
      PlacementUtils.register($$0, BAMBOO_VEGETATION, $$59, treePlacement(PlacementUtils.countExtra(30, 0.1F, 1)));
      PlacementUtils.register($$0, MUSHROOM_ISLAND_VEGETATION, $$60, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
      PlacementUtils.register(
         $$0,
         TREES_MANGROVE,
         $$61,
         CountPlacement.of(25),
         InSquarePlacement.spread(),
         SurfaceWaterDepthFilter.forMaxDepth(5),
         PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
         BiomeFilter.biome()
      );
   }
}
