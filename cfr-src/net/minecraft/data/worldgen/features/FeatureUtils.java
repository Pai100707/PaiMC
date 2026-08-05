/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.AquaticFeatures;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.features.PileFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> $$0) {
        AquaticFeatures.bootstrap($$0);
        CaveFeatures.bootstrap($$0);
        EndFeatures.bootstrap($$0);
        MiscOverworldFeatures.bootstrap($$0);
        NetherFeatures.bootstrap($$0);
        OreFeatures.bootstrap($$0);
        PileFeatures.bootstrap($$0);
        TreeFeatures.bootstrap($$0);
        VegetationFeatures.bootstrap($$0);
    }

    private static BlockPredicate simplePatchPredicate(List<Block> $$0) {
        BlockPredicate $$2;
        if (!$$0.isEmpty()) {
            BlockPredicate $$1 = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(Direction.DOWN.getUnitVec3i(), $$0));
        } else {
            $$2 = BlockPredicate.ONLY_IN_AIR_PREDICATE;
        }
        return $$2;
    }

    public static RandomPatchConfiguration simpleRandomPatchConfiguration(int $$0, Holder<PlacedFeature> $$1) {
        return new RandomPatchConfiguration($$0, 7, 3, $$1);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F $$0, FC $$1, List<Block> $$2, int $$3) {
        return FeatureUtils.simpleRandomPatchConfiguration($$3, PlacementUtils.filtered($$0, $$1, FeatureUtils.simplePatchPredicate($$2)));
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F $$0, FC $$1, List<Block> $$2) {
        return FeatureUtils.simplePatchConfiguration($$0, $$1, $$2, 96);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F $$0, FC $$1) {
        return FeatureUtils.simplePatchConfiguration($$0, $$1, List.of(), 96);
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String $$0) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.withDefaultNamespace($$0));
    }

    public static void register(BootstrapContext<ConfiguredFeature<?, ?>> $$0, ResourceKey<ConfiguredFeature<?, ?>> $$1, Feature<NoneFeatureConfiguration> $$2) {
        FeatureUtils.register($$0, $$1, $$2, FeatureConfiguration.NONE);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> $$0, ResourceKey<ConfiguredFeature<?, ?>> $$1, F $$2, FC $$3) {
        $$0.register($$1, new ConfiguredFeature<FC, F>($$2, $$3));
    }
}

