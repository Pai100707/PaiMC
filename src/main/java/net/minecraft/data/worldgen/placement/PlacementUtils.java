package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class PlacementUtils {
   public static final PlacementModifier HEIGHTMAP = HeightmapPlacement.onHeightmap(Types.MOTION_BLOCKING);
   public static final PlacementModifier HEIGHTMAP_NO_LEAVES = HeightmapPlacement.onHeightmap(Types.MOTION_BLOCKING_NO_LEAVES);
   public static final PlacementModifier HEIGHTMAP_TOP_SOLID = HeightmapPlacement.onHeightmap(Types.OCEAN_FLOOR_WG);
   public static final PlacementModifier HEIGHTMAP_WORLD_SURFACE = HeightmapPlacement.onHeightmap(Types.WORLD_SURFACE_WG);
   public static final PlacementModifier HEIGHTMAP_OCEAN_FLOOR = HeightmapPlacement.onHeightmap(Types.OCEAN_FLOOR);
   public static final PlacementModifier FULL_RANGE = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top());
   public static final PlacementModifier RANGE_10_10 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(10), VerticalAnchor.belowTop(10));
   public static final PlacementModifier RANGE_8_8 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.belowTop(8));
   public static final PlacementModifier RANGE_4_4 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(4), VerticalAnchor.belowTop(4));
   public static final PlacementModifier RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT = HeightRangePlacement.uniform(
      VerticalAnchor.bottom(), VerticalAnchor.absolute(256)
   );

   public static void bootstrap(BootstrapContext<PlacedFeature> $$0) {
      AquaticPlacements.bootstrap($$0);
      CavePlacements.bootstrap($$0);
      EndPlacements.bootstrap($$0);
      MiscOverworldPlacements.bootstrap($$0);
      NetherPlacements.bootstrap($$0);
      OrePlacements.bootstrap($$0);
      TreePlacements.bootstrap($$0);
      VegetationPlacements.bootstrap($$0);
      VillagePlacements.bootstrap($$0);
   }

   public static ResourceKey<PlacedFeature> createKey(String $$0) {
      return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.withDefaultNamespace($$0));
   }

   public static void register(
      BootstrapContext<PlacedFeature> $$0, ResourceKey<PlacedFeature> $$1, Holder<ConfiguredFeature<?, ?>> $$2, List<PlacementModifier> $$3
   ) {
      $$0.register($$1, new PlacedFeature($$2, List.copyOf($$3)));
   }

   public static void register(
      BootstrapContext<PlacedFeature> $$0, ResourceKey<PlacedFeature> $$1, Holder<ConfiguredFeature<?, ?>> $$2, PlacementModifier... $$3
   ) {
      register($$0, $$1, $$2, List.of($$3));
   }

   public static PlacementModifier countExtra(int $$0, float $$1, int $$2) {
      float $$3 = 1.0F / $$1;
      if (Math.abs($$3 - (int)$$3) > 1.0E-5F) {
         throw new IllegalStateException("Chance data cannot be represented as list weight");
      } else {
         WeightedList<IntProvider> $$4 = WeightedList.builder().add(ConstantInt.of($$0), (int)$$3 - 1).add(ConstantInt.of($$0 + $$2), 1).build();
         return CountPlacement.of(new WeightedListInt($$4));
      }
   }

   public static PlacementFilter isEmpty() {
      return BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE);
   }

   public static BlockPredicateFilter filteredByBlockSurvival(Block $$0) {
      return BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive($$0.defaultBlockState(), BlockPos.ZERO));
   }

   public static Holder<PlacedFeature> inlinePlaced(Holder<ConfiguredFeature<?, ?>> $$0, PlacementModifier... $$1) {
      return Holder.direct(new PlacedFeature($$0, List.of($$1)));
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> inlinePlaced(F $$0, FC $$1, PlacementModifier... $$2) {
      return inlinePlaced(Holder.direct(new ConfiguredFeature($$0, $$1)), $$2);
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> onlyWhenEmpty(F $$0, FC $$1) {
      return filtered($$0, $$1, BlockPredicate.ONLY_IN_AIR_PREDICATE);
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> filtered(F $$0, FC $$1, BlockPredicate $$2) {
      return inlinePlaced($$0, $$1, BlockPredicateFilter.forPredicate($$2));
   }
}
