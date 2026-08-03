package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.AquaticFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class AquaticPlacements {
   public static final ResourceKey<PlacedFeature> SEAGRASS_WARM = PlacementUtils.createKey("seagrass_warm");
   public static final ResourceKey<PlacedFeature> SEAGRASS_NORMAL = PlacementUtils.createKey("seagrass_normal");
   public static final ResourceKey<PlacedFeature> SEAGRASS_COLD = PlacementUtils.createKey("seagrass_cold");
   public static final ResourceKey<PlacedFeature> SEAGRASS_RIVER = PlacementUtils.createKey("seagrass_river");
   public static final ResourceKey<PlacedFeature> SEAGRASS_SWAMP = PlacementUtils.createKey("seagrass_swamp");
   public static final ResourceKey<PlacedFeature> SEAGRASS_DEEP_WARM = PlacementUtils.createKey("seagrass_deep_warm");
   public static final ResourceKey<PlacedFeature> SEAGRASS_DEEP = PlacementUtils.createKey("seagrass_deep");
   public static final ResourceKey<PlacedFeature> SEAGRASS_DEEP_COLD = PlacementUtils.createKey("seagrass_deep_cold");
   public static final ResourceKey<PlacedFeature> SEA_PICKLE = PlacementUtils.createKey("sea_pickle");
   public static final ResourceKey<PlacedFeature> KELP_COLD = PlacementUtils.createKey("kelp_cold");
   public static final ResourceKey<PlacedFeature> KELP_WARM = PlacementUtils.createKey("kelp_warm");
   public static final ResourceKey<PlacedFeature> WARM_OCEAN_VEGETATION = PlacementUtils.createKey("warm_ocean_vegetation");

   private static List<PlacementModifier> seagrassPlacement(int $$0) {
      return List.of(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, CountPlacement.of($$0), BiomeFilter.biome());
   }

   public static void bootstrap(BootstrapContext<PlacedFeature> $$0) {
      HolderGetter<ConfiguredFeature<?, ?>> $$1 = $$0.lookup(Registries.CONFIGURED_FEATURE);
      Reference<ConfiguredFeature<?, ?>> $$2 = $$1.getOrThrow(AquaticFeatures.SEAGRASS_SHORT);
      Reference<ConfiguredFeature<?, ?>> $$3 = $$1.getOrThrow(AquaticFeatures.SEAGRASS_SLIGHTLY_LESS_SHORT);
      Reference<ConfiguredFeature<?, ?>> $$4 = $$1.getOrThrow(AquaticFeatures.SEAGRASS_MID);
      Reference<ConfiguredFeature<?, ?>> $$5 = $$1.getOrThrow(AquaticFeatures.SEAGRASS_TALL);
      Reference<ConfiguredFeature<?, ?>> $$6 = $$1.getOrThrow(AquaticFeatures.SEA_PICKLE);
      Reference<ConfiguredFeature<?, ?>> $$7 = $$1.getOrThrow(AquaticFeatures.KELP);
      Reference<ConfiguredFeature<?, ?>> $$8 = $$1.getOrThrow(AquaticFeatures.WARM_OCEAN_VEGETATION);
      PlacementUtils.register($$0, SEAGRASS_WARM, $$2, seagrassPlacement(80));
      PlacementUtils.register($$0, SEAGRASS_NORMAL, $$2, seagrassPlacement(48));
      PlacementUtils.register($$0, SEAGRASS_COLD, $$2, seagrassPlacement(32));
      PlacementUtils.register($$0, SEAGRASS_RIVER, $$3, seagrassPlacement(48));
      PlacementUtils.register($$0, SEAGRASS_SWAMP, $$4, seagrassPlacement(64));
      PlacementUtils.register($$0, SEAGRASS_DEEP_WARM, $$5, seagrassPlacement(80));
      PlacementUtils.register($$0, SEAGRASS_DEEP, $$5, seagrassPlacement(48));
      PlacementUtils.register($$0, SEAGRASS_DEEP_COLD, $$5, seagrassPlacement(40));
      PlacementUtils.register(
         $$0, SEA_PICKLE, $$6, RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, KELP_COLD, $$7, NoiseBasedCountPlacement.of(120, 80.0, 0.0), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0, KELP_WARM, $$7, NoiseBasedCountPlacement.of(80, 80.0, 0.0), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome()
      );
      PlacementUtils.register(
         $$0,
         WARM_OCEAN_VEGETATION,
         $$8,
         NoiseBasedCountPlacement.of(20, 400.0, 0.0),
         InSquarePlacement.spread(),
         PlacementUtils.HEIGHTMAP_TOP_SOLID,
         BiomeFilter.biome()
      );
   }
}
