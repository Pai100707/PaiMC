package net.minecraft.data.worldgen.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.FixedPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class EndPlacements {
   public static final ResourceKey<PlacedFeature> END_PLATFORM = PlacementUtils.createKey("end_platform");
   public static final ResourceKey<PlacedFeature> END_SPIKE = PlacementUtils.createKey("end_spike");
   public static final ResourceKey<PlacedFeature> END_GATEWAY_RETURN = PlacementUtils.createKey("end_gateway_return");
   public static final ResourceKey<PlacedFeature> CHORUS_PLANT = PlacementUtils.createKey("chorus_plant");
   public static final ResourceKey<PlacedFeature> END_ISLAND_DECORATED = PlacementUtils.createKey("end_island_decorated");

   public static void bootstrap(BootstrapContext<PlacedFeature> $$0) {
      HolderGetter<ConfiguredFeature<?, ?>> $$1 = $$0.lookup(Registries.CONFIGURED_FEATURE);
      Holder<ConfiguredFeature<?, ?>> $$2 = $$1.getOrThrow(EndFeatures.END_PLATFORM);
      Holder<ConfiguredFeature<?, ?>> $$3 = $$1.getOrThrow(EndFeatures.END_SPIKE);
      Holder<ConfiguredFeature<?, ?>> $$4 = $$1.getOrThrow(EndFeatures.END_GATEWAY_RETURN);
      Holder<ConfiguredFeature<?, ?>> $$5 = $$1.getOrThrow(EndFeatures.CHORUS_PLANT);
      Holder<ConfiguredFeature<?, ?>> $$6 = $$1.getOrThrow(EndFeatures.END_ISLAND);
      PlacementUtils.register($$0, END_PLATFORM, $$2, FixedPlacement.of(new BlockPos[]{ServerLevel.END_SPAWN_POINT.below()}), BiomeFilter.biome());
      PlacementUtils.register($$0, END_SPIKE, $$3, BiomeFilter.biome());
      PlacementUtils.register(
         $$0,
         END_GATEWAY_RETURN,
         $$4,
         RarityFilter.onAverageOnceEvery(700),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP,
         RandomOffsetPlacement.vertical(UniformInt.of(3, 9)),
         BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, CHORUS_PLANT, $$5, CountPlacement.of(UniformInt.of(0, 4)), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         END_ISLAND_DECORATED,
         $$6,
         RarityFilter.onAverageOnceEvery(14),
         PlacementUtils.countExtra(1, 0.25F, 1),
         InSquarePlacement.spread(),
         HeightRangePlacement.uniform(VerticalAnchor.absolute(55), VerticalAnchor.absolute(70)),
         BiomeFilter.biome()
      );
   }
}
