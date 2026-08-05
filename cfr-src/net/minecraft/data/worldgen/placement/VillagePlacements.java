/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.placement;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.PileFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class VillagePlacements {
    public static final ResourceKey<PlacedFeature> PILE_HAY_VILLAGE = PlacementUtils.createKey("pile_hay");
    public static final ResourceKey<PlacedFeature> PILE_MELON_VILLAGE = PlacementUtils.createKey("pile_melon");
    public static final ResourceKey<PlacedFeature> PILE_SNOW_VILLAGE = PlacementUtils.createKey("pile_snow");
    public static final ResourceKey<PlacedFeature> PILE_ICE_VILLAGE = PlacementUtils.createKey("pile_ice");
    public static final ResourceKey<PlacedFeature> PILE_PUMPKIN_VILLAGE = PlacementUtils.createKey("pile_pumpkin");
    public static final ResourceKey<PlacedFeature> OAK_VILLAGE = PlacementUtils.createKey("oak");
    public static final ResourceKey<PlacedFeature> ACACIA_VILLAGE = PlacementUtils.createKey("acacia");
    public static final ResourceKey<PlacedFeature> SPRUCE_VILLAGE = PlacementUtils.createKey("spruce");
    public static final ResourceKey<PlacedFeature> PINE_VILLAGE = PlacementUtils.createKey("pine");
    public static final ResourceKey<PlacedFeature> PATCH_CACTUS_VILLAGE = PlacementUtils.createKey("patch_cactus");
    public static final ResourceKey<PlacedFeature> FLOWER_PLAIN_VILLAGE = PlacementUtils.createKey("flower_plain");
    public static final ResourceKey<PlacedFeature> PATCH_TAIGA_GRASS_VILLAGE = PlacementUtils.createKey("patch_taiga_grass");
    public static final ResourceKey<PlacedFeature> PATCH_BERRY_BUSH_VILLAGE = PlacementUtils.createKey("patch_berry_bush");

    public static void bootstrap(BootstrapContext<PlacedFeature> $$0) {
        HolderGetter<ConfiguredFeature<?, ?>> $$1 = $$0.lookup(Registries.CONFIGURED_FEATURE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$2 = $$1.getOrThrow(PileFeatures.PILE_HAY);
        Holder.Reference<ConfiguredFeature<?, ?>> $$3 = $$1.getOrThrow(PileFeatures.PILE_MELON);
        Holder.Reference<ConfiguredFeature<?, ?>> $$4 = $$1.getOrThrow(PileFeatures.PILE_SNOW);
        Holder.Reference<ConfiguredFeature<?, ?>> $$5 = $$1.getOrThrow(PileFeatures.PILE_ICE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$6 = $$1.getOrThrow(PileFeatures.PILE_PUMPKIN);
        Holder.Reference<ConfiguredFeature<?, ?>> $$7 = $$1.getOrThrow(TreeFeatures.OAK);
        Holder.Reference<ConfiguredFeature<?, ?>> $$8 = $$1.getOrThrow(TreeFeatures.ACACIA);
        Holder.Reference<ConfiguredFeature<?, ?>> $$9 = $$1.getOrThrow(TreeFeatures.SPRUCE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$10 = $$1.getOrThrow(TreeFeatures.PINE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$11 = $$1.getOrThrow(VegetationFeatures.PATCH_CACTUS);
        Holder.Reference<ConfiguredFeature<?, ?>> $$12 = $$1.getOrThrow(VegetationFeatures.FLOWER_PLAIN);
        Holder.Reference<ConfiguredFeature<?, ?>> $$13 = $$1.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
        Holder.Reference<ConfiguredFeature<?, ?>> $$14 = $$1.getOrThrow(VegetationFeatures.PATCH_BERRY_BUSH);
        PlacementUtils.register($$0, PILE_HAY_VILLAGE, $$2, new PlacementModifier[0]);
        PlacementUtils.register($$0, PILE_MELON_VILLAGE, $$3, new PlacementModifier[0]);
        PlacementUtils.register($$0, PILE_SNOW_VILLAGE, $$4, new PlacementModifier[0]);
        PlacementUtils.register($$0, PILE_ICE_VILLAGE, $$5, new PlacementModifier[0]);
        PlacementUtils.register($$0, PILE_PUMPKIN_VILLAGE, $$6, new PlacementModifier[0]);
        PlacementUtils.register($$0, OAK_VILLAGE, $$7, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register($$0, ACACIA_VILLAGE, $$8, PlacementUtils.filteredByBlockSurvival(Blocks.ACACIA_SAPLING));
        PlacementUtils.register($$0, SPRUCE_VILLAGE, $$9, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
        PlacementUtils.register($$0, PINE_VILLAGE, $$10, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
        PlacementUtils.register($$0, PATCH_CACTUS_VILLAGE, $$11, new PlacementModifier[0]);
        PlacementUtils.register($$0, FLOWER_PLAIN_VILLAGE, $$12, new PlacementModifier[0]);
        PlacementUtils.register($$0, PATCH_TAIGA_GRASS_VILLAGE, $$13, new PlacementModifier[0]);
        PlacementUtils.register($$0, PATCH_BERRY_BUSH_VILLAGE, $$14, new PlacementModifier[0]);
    }
}

