/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.flag;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagRegistry;
import net.minecraft.world.flag.FeatureFlagSet;

public class FeatureFlags {
    public static final FeatureFlag VANILLA;
    public static final FeatureFlag TRADE_REBALANCE;
    public static final FeatureFlag REDSTONE_EXPERIMENTS;
    public static final FeatureFlag MINECART_IMPROVEMENTS;
    public static final FeatureFlagRegistry REGISTRY;
    public static final Codec<FeatureFlagSet> CODEC;
    public static final FeatureFlagSet VANILLA_SET;
    public static final FeatureFlagSet DEFAULT_FLAGS;

    public static String printMissingFlags(FeatureFlagSet $$0, FeatureFlagSet $$1) {
        return FeatureFlags.printMissingFlags(REGISTRY, $$0, $$1);
    }

    public static String printMissingFlags(FeatureFlagRegistry $$0, FeatureFlagSet $$12, FeatureFlagSet $$2) {
        Set<Identifier> $$3 = $$0.toNames($$2);
        Set<Identifier> $$4 = $$0.toNames($$12);
        return $$3.stream().filter($$1 -> !$$4.contains($$1)).map(Identifier::toString).collect(Collectors.joining(", "));
    }

    public static boolean isExperimental(FeatureFlagSet $$0) {
        return !$$0.isSubsetOf(VANILLA_SET);
    }

    static {
        FeatureFlagRegistry.Builder $$0 = new FeatureFlagRegistry.Builder("main");
        VANILLA = $$0.createVanilla("vanilla");
        TRADE_REBALANCE = $$0.createVanilla("trade_rebalance");
        REDSTONE_EXPERIMENTS = $$0.createVanilla("redstone_experiments");
        MINECART_IMPROVEMENTS = $$0.createVanilla("minecart_improvements");
        REGISTRY = $$0.build();
        CODEC = REGISTRY.codec();
        DEFAULT_FLAGS = VANILLA_SET = FeatureFlagSet.of(VANILLA);
    }
}

