package net.minecraft.world.flag;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;

public class FeatureFlags {
   public static final net.minecraft.world.flag.FeatureFlag VANILLA;
   public static final net.minecraft.world.flag.FeatureFlag TRADE_REBALANCE;
   public static final net.minecraft.world.flag.FeatureFlag REDSTONE_EXPERIMENTS;
   public static final net.minecraft.world.flag.FeatureFlag MINECART_IMPROVEMENTS;
   public static final net.minecraft.world.flag.FeatureFlagRegistry REGISTRY;
   public static final Codec<net.minecraft.world.flag.FeatureFlagSet> CODEC;
   public static final net.minecraft.world.flag.FeatureFlagSet VANILLA_SET;
   public static final net.minecraft.world.flag.FeatureFlagSet DEFAULT_FLAGS;

   public static String printMissingFlags(net.minecraft.world.flag.FeatureFlagSet $$0, net.minecraft.world.flag.FeatureFlagSet $$1) {
      return printMissingFlags(REGISTRY, $$0, $$1);
   }

   public static String printMissingFlags(
      net.minecraft.world.flag.FeatureFlagRegistry $$0, net.minecraft.world.flag.FeatureFlagSet $$1, net.minecraft.world.flag.FeatureFlagSet $$2
   ) {
      Set<Identifier> $$3 = $$0.toNames($$2);
      Set<Identifier> $$4 = $$0.toNames($$1);
      return $$3.stream().filter($$1x -> !$$4.contains($$1x)).<CharSequence>map(Identifier::toString).collect(Collectors.joining(", "));
   }

   public static boolean isExperimental(net.minecraft.world.flag.FeatureFlagSet $$0) {
      return !$$0.isSubsetOf(VANILLA_SET);
   }

   static {
      net.minecraft.world.flag.FeatureFlagRegistry.Builder $$0 = new net.minecraft.world.flag.FeatureFlagRegistry.Builder("main");
      VANILLA = $$0.createVanilla("vanilla");
      TRADE_REBALANCE = $$0.createVanilla("trade_rebalance");
      REDSTONE_EXPERIMENTS = $$0.createVanilla("redstone_experiments");
      MINECART_IMPROVEMENTS = $$0.createVanilla("minecart_improvements");
      REGISTRY = $$0.build();
      CODEC = REGISTRY.codec();
      VANILLA_SET = net.minecraft.world.flag.FeatureFlagSet.of(VANILLA);
      DEFAULT_FLAGS = VANILLA_SET;
   }
}
