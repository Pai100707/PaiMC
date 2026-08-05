/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class OrePlacements {
    public static final ResourceKey<PlacedFeature> ORE_MAGMA = PlacementUtils.createKey("ore_magma");
    public static final ResourceKey<PlacedFeature> ORE_SOUL_SAND = PlacementUtils.createKey("ore_soul_sand");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_DELTAS = PlacementUtils.createKey("ore_gold_deltas");
    public static final ResourceKey<PlacedFeature> ORE_QUARTZ_DELTAS = PlacementUtils.createKey("ore_quartz_deltas");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_NETHER = PlacementUtils.createKey("ore_gold_nether");
    public static final ResourceKey<PlacedFeature> ORE_QUARTZ_NETHER = PlacementUtils.createKey("ore_quartz_nether");
    public static final ResourceKey<PlacedFeature> ORE_GRAVEL_NETHER = PlacementUtils.createKey("ore_gravel_nether");
    public static final ResourceKey<PlacedFeature> ORE_BLACKSTONE = PlacementUtils.createKey("ore_blackstone");
    public static final ResourceKey<PlacedFeature> ORE_DIRT = PlacementUtils.createKey("ore_dirt");
    public static final ResourceKey<PlacedFeature> ORE_GRAVEL = PlacementUtils.createKey("ore_gravel");
    public static final ResourceKey<PlacedFeature> ORE_GRANITE_UPPER = PlacementUtils.createKey("ore_granite_upper");
    public static final ResourceKey<PlacedFeature> ORE_GRANITE_LOWER = PlacementUtils.createKey("ore_granite_lower");
    public static final ResourceKey<PlacedFeature> ORE_DIORITE_UPPER = PlacementUtils.createKey("ore_diorite_upper");
    public static final ResourceKey<PlacedFeature> ORE_DIORITE_LOWER = PlacementUtils.createKey("ore_diorite_lower");
    public static final ResourceKey<PlacedFeature> ORE_ANDESITE_UPPER = PlacementUtils.createKey("ore_andesite_upper");
    public static final ResourceKey<PlacedFeature> ORE_ANDESITE_LOWER = PlacementUtils.createKey("ore_andesite_lower");
    public static final ResourceKey<PlacedFeature> ORE_TUFF = PlacementUtils.createKey("ore_tuff");
    public static final ResourceKey<PlacedFeature> ORE_COAL_UPPER = PlacementUtils.createKey("ore_coal_upper");
    public static final ResourceKey<PlacedFeature> ORE_COAL_LOWER = PlacementUtils.createKey("ore_coal_lower");
    public static final ResourceKey<PlacedFeature> ORE_IRON_UPPER = PlacementUtils.createKey("ore_iron_upper");
    public static final ResourceKey<PlacedFeature> ORE_IRON_MIDDLE = PlacementUtils.createKey("ore_iron_middle");
    public static final ResourceKey<PlacedFeature> ORE_IRON_SMALL = PlacementUtils.createKey("ore_iron_small");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_EXTRA = PlacementUtils.createKey("ore_gold_extra");
    public static final ResourceKey<PlacedFeature> ORE_GOLD = PlacementUtils.createKey("ore_gold");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_LOWER = PlacementUtils.createKey("ore_gold_lower");
    public static final ResourceKey<PlacedFeature> ORE_REDSTONE = PlacementUtils.createKey("ore_redstone");
    public static final ResourceKey<PlacedFeature> ORE_REDSTONE_LOWER = PlacementUtils.createKey("ore_redstone_lower");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND = PlacementUtils.createKey("ore_diamond");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND_MEDIUM = PlacementUtils.createKey("ore_diamond_medium");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND_LARGE = PlacementUtils.createKey("ore_diamond_large");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND_BURIED = PlacementUtils.createKey("ore_diamond_buried");
    public static final ResourceKey<PlacedFeature> ORE_LAPIS = PlacementUtils.createKey("ore_lapis");
    public static final ResourceKey<PlacedFeature> ORE_LAPIS_BURIED = PlacementUtils.createKey("ore_lapis_buried");
    public static final ResourceKey<PlacedFeature> ORE_INFESTED = PlacementUtils.createKey("ore_infested");
    public static final ResourceKey<PlacedFeature> ORE_EMERALD = PlacementUtils.createKey("ore_emerald");
    public static final ResourceKey<PlacedFeature> ORE_ANCIENT_DEBRIS_LARGE = PlacementUtils.createKey("ore_ancient_debris_large");
    public static final ResourceKey<PlacedFeature> ORE_ANCIENT_DEBRIS_SMALL = PlacementUtils.createKey("ore_debris_small");
    public static final ResourceKey<PlacedFeature> ORE_COPPER = PlacementUtils.createKey("ore_copper");
    public static final ResourceKey<PlacedFeature> ORE_COPPER_LARGE = PlacementUtils.createKey("ore_copper_large");
    public static final ResourceKey<PlacedFeature> ORE_CLAY = PlacementUtils.createKey("ore_clay");

    private static List<PlacementModifier> orePlacement(PlacementModifier $$0, PlacementModifier $$1) {
        return List.of($$0, InSquarePlacement.spread(), $$1, BiomeFilter.biome());
    }

    private static List<PlacementModifier> commonOrePlacement(int $$0, PlacementModifier $$1) {
        return OrePlacements.orePlacement(CountPlacement.of($$0), $$1);
    }

    private static List<PlacementModifier> rareOrePlacement(int $$0, PlacementModifier $$1) {
        return OrePlacements.orePlacement(RarityFilter.onAverageOnceEvery($$0), $$1);
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> $$0) {
        HolderGetter<ConfiguredFeature<?, ?>> $$1 = $$0.lookup(Registries.CONFIGURED_FEATURE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$2 = $$1.getOrThrow(OreFeatures.ORE_MAGMA);
        Holder.Reference<ConfiguredFeature<?, ?>> $$3 = $$1.getOrThrow(OreFeatures.ORE_SOUL_SAND);
        Holder.Reference<ConfiguredFeature<?, ?>> $$4 = $$1.getOrThrow(OreFeatures.ORE_NETHER_GOLD);
        Holder.Reference<ConfiguredFeature<?, ?>> $$5 = $$1.getOrThrow(OreFeatures.ORE_QUARTZ);
        Holder.Reference<ConfiguredFeature<?, ?>> $$6 = $$1.getOrThrow(OreFeatures.ORE_GRAVEL_NETHER);
        Holder.Reference<ConfiguredFeature<?, ?>> $$7 = $$1.getOrThrow(OreFeatures.ORE_BLACKSTONE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$8 = $$1.getOrThrow(OreFeatures.ORE_DIRT);
        Holder.Reference<ConfiguredFeature<?, ?>> $$9 = $$1.getOrThrow(OreFeatures.ORE_GRAVEL);
        Holder.Reference<ConfiguredFeature<?, ?>> $$10 = $$1.getOrThrow(OreFeatures.ORE_GRANITE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$11 = $$1.getOrThrow(OreFeatures.ORE_DIORITE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$12 = $$1.getOrThrow(OreFeatures.ORE_ANDESITE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$13 = $$1.getOrThrow(OreFeatures.ORE_TUFF);
        Holder.Reference<ConfiguredFeature<?, ?>> $$14 = $$1.getOrThrow(OreFeatures.ORE_COAL);
        Holder.Reference<ConfiguredFeature<?, ?>> $$15 = $$1.getOrThrow(OreFeatures.ORE_COAL_BURIED);
        Holder.Reference<ConfiguredFeature<?, ?>> $$16 = $$1.getOrThrow(OreFeatures.ORE_IRON);
        Holder.Reference<ConfiguredFeature<?, ?>> $$17 = $$1.getOrThrow(OreFeatures.ORE_IRON_SMALL);
        Holder.Reference<ConfiguredFeature<?, ?>> $$18 = $$1.getOrThrow(OreFeatures.ORE_GOLD);
        Holder.Reference<ConfiguredFeature<?, ?>> $$19 = $$1.getOrThrow(OreFeatures.ORE_GOLD_BURIED);
        Holder.Reference<ConfiguredFeature<?, ?>> $$20 = $$1.getOrThrow(OreFeatures.ORE_REDSTONE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$21 = $$1.getOrThrow(OreFeatures.ORE_DIAMOND_SMALL);
        Holder.Reference<ConfiguredFeature<?, ?>> $$22 = $$1.getOrThrow(OreFeatures.ORE_DIAMOND_MEDIUM);
        Holder.Reference<ConfiguredFeature<?, ?>> $$23 = $$1.getOrThrow(OreFeatures.ORE_DIAMOND_LARGE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$24 = $$1.getOrThrow(OreFeatures.ORE_DIAMOND_BURIED);
        Holder.Reference<ConfiguredFeature<?, ?>> $$25 = $$1.getOrThrow(OreFeatures.ORE_LAPIS);
        Holder.Reference<ConfiguredFeature<?, ?>> $$26 = $$1.getOrThrow(OreFeatures.ORE_LAPIS_BURIED);
        Holder.Reference<ConfiguredFeature<?, ?>> $$27 = $$1.getOrThrow(OreFeatures.ORE_INFESTED);
        Holder.Reference<ConfiguredFeature<?, ?>> $$28 = $$1.getOrThrow(OreFeatures.ORE_EMERALD);
        Holder.Reference<ConfiguredFeature<?, ?>> $$29 = $$1.getOrThrow(OreFeatures.ORE_ANCIENT_DEBRIS_LARGE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$30 = $$1.getOrThrow(OreFeatures.ORE_ANCIENT_DEBRIS_SMALL);
        Holder.Reference<ConfiguredFeature<?, ?>> $$31 = $$1.getOrThrow(OreFeatures.ORE_COPPPER_SMALL);
        Holder.Reference<ConfiguredFeature<?, ?>> $$32 = $$1.getOrThrow(OreFeatures.ORE_COPPER_LARGE);
        Holder.Reference<ConfiguredFeature<?, ?>> $$33 = $$1.getOrThrow(OreFeatures.ORE_CLAY);
        PlacementUtils.register($$0, ORE_MAGMA, $$2, OrePlacements.commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.absolute(27), VerticalAnchor.absolute(36))));
        PlacementUtils.register($$0, ORE_SOUL_SAND, $$3, OrePlacements.commonOrePlacement(12, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31))));
        PlacementUtils.register($$0, ORE_GOLD_DELTAS, $$4, OrePlacements.commonOrePlacement(20, PlacementUtils.RANGE_10_10));
        PlacementUtils.register($$0, ORE_QUARTZ_DELTAS, $$5, OrePlacements.commonOrePlacement(32, PlacementUtils.RANGE_10_10));
        PlacementUtils.register($$0, ORE_GOLD_NETHER, $$4, OrePlacements.commonOrePlacement(10, PlacementUtils.RANGE_10_10));
        PlacementUtils.register($$0, ORE_QUARTZ_NETHER, $$5, OrePlacements.commonOrePlacement(16, PlacementUtils.RANGE_10_10));
        PlacementUtils.register($$0, ORE_GRAVEL_NETHER, $$6, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(41))));
        PlacementUtils.register($$0, ORE_BLACKSTONE, $$7, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(31))));
        PlacementUtils.register($$0, ORE_DIRT, $$8, OrePlacements.commonOrePlacement(7, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(160))));
        PlacementUtils.register($$0, ORE_GRAVEL, $$9, OrePlacements.commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top())));
        PlacementUtils.register($$0, ORE_GRANITE_UPPER, $$10, OrePlacements.rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
        PlacementUtils.register($$0, ORE_GRANITE_LOWER, $$10, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));
        PlacementUtils.register($$0, ORE_DIORITE_UPPER, $$11, OrePlacements.rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
        PlacementUtils.register($$0, ORE_DIORITE_LOWER, $$11, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));
        PlacementUtils.register($$0, ORE_ANDESITE_UPPER, $$12, OrePlacements.rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
        PlacementUtils.register($$0, ORE_ANDESITE_LOWER, $$12, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));
        PlacementUtils.register($$0, ORE_TUFF, $$13, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(0))));
        PlacementUtils.register($$0, ORE_COAL_UPPER, $$14, OrePlacements.commonOrePlacement(30, HeightRangePlacement.uniform(VerticalAnchor.absolute(136), VerticalAnchor.top())));
        PlacementUtils.register($$0, ORE_COAL_LOWER, $$15, OrePlacements.commonOrePlacement(20, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(192))));
        PlacementUtils.register($$0, ORE_IRON_UPPER, $$16, OrePlacements.commonOrePlacement(90, HeightRangePlacement.triangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384))));
        PlacementUtils.register($$0, ORE_IRON_MIDDLE, $$16, OrePlacements.commonOrePlacement(10, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56))));
        PlacementUtils.register($$0, ORE_IRON_SMALL, $$17, OrePlacements.commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72))));
        PlacementUtils.register($$0, ORE_GOLD_EXTRA, $$18, OrePlacements.commonOrePlacement(50, HeightRangePlacement.uniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(256))));
        PlacementUtils.register($$0, ORE_GOLD, $$19, OrePlacements.commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32))));
        PlacementUtils.register($$0, ORE_GOLD_LOWER, $$19, OrePlacements.orePlacement(CountPlacement.of(UniformInt.of(0, 1)), HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-48))));
        PlacementUtils.register($$0, ORE_REDSTONE, $$20, OrePlacements.commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(15))));
        PlacementUtils.register($$0, ORE_REDSTONE_LOWER, $$20, OrePlacements.commonOrePlacement(8, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-32), VerticalAnchor.aboveBottom(32))));
        PlacementUtils.register($$0, ORE_DIAMOND, $$21, OrePlacements.commonOrePlacement(7, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))));
        PlacementUtils.register($$0, ORE_DIAMOND_MEDIUM, $$22, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-4))));
        PlacementUtils.register($$0, ORE_DIAMOND_LARGE, $$23, OrePlacements.rareOrePlacement(9, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))));
        PlacementUtils.register($$0, ORE_DIAMOND_BURIED, $$24, OrePlacements.commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))));
        PlacementUtils.register($$0, ORE_LAPIS, $$25, OrePlacements.commonOrePlacement(2, HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(32))));
        PlacementUtils.register($$0, ORE_LAPIS_BURIED, $$26, OrePlacements.commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64))));
        PlacementUtils.register($$0, ORE_INFESTED, $$27, OrePlacements.commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(63))));
        PlacementUtils.register($$0, ORE_EMERALD, $$28, OrePlacements.commonOrePlacement(100, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(480))));
        PlacementUtils.register($$0, ORE_ANCIENT_DEBRIS_LARGE, $$29, InSquarePlacement.spread(), HeightRangePlacement.triangle(VerticalAnchor.absolute(8), VerticalAnchor.absolute(24)), BiomeFilter.biome());
        PlacementUtils.register($$0, ORE_ANCIENT_DEBRIS_SMALL, $$30, InSquarePlacement.spread(), PlacementUtils.RANGE_8_8, BiomeFilter.biome());
        PlacementUtils.register($$0, ORE_COPPER, $$31, OrePlacements.commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112))));
        PlacementUtils.register($$0, ORE_COPPER_LARGE, $$32, OrePlacements.commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112))));
        PlacementUtils.register($$0, ORE_CLAY, $$33, OrePlacements.commonOrePlacement(46, PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT));
    }
}

