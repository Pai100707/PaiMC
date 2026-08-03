package net.minecraft.world.item.equipment.trim;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;

public class TrimPatterns {
   public static final ResourceKey<TrimPattern> SENTRY = registryKey("sentry");
   public static final ResourceKey<TrimPattern> DUNE = registryKey("dune");
   public static final ResourceKey<TrimPattern> COAST = registryKey("coast");
   public static final ResourceKey<TrimPattern> WILD = registryKey("wild");
   public static final ResourceKey<TrimPattern> WARD = registryKey("ward");
   public static final ResourceKey<TrimPattern> EYE = registryKey("eye");
   public static final ResourceKey<TrimPattern> VEX = registryKey("vex");
   public static final ResourceKey<TrimPattern> TIDE = registryKey("tide");
   public static final ResourceKey<TrimPattern> SNOUT = registryKey("snout");
   public static final ResourceKey<TrimPattern> RIB = registryKey("rib");
   public static final ResourceKey<TrimPattern> SPIRE = registryKey("spire");
   public static final ResourceKey<TrimPattern> WAYFINDER = registryKey("wayfinder");
   public static final ResourceKey<TrimPattern> SHAPER = registryKey("shaper");
   public static final ResourceKey<TrimPattern> SILENCE = registryKey("silence");
   public static final ResourceKey<TrimPattern> RAISER = registryKey("raiser");
   public static final ResourceKey<TrimPattern> HOST = registryKey("host");
   public static final ResourceKey<TrimPattern> FLOW = registryKey("flow");
   public static final ResourceKey<TrimPattern> BOLT = registryKey("bolt");

   public static void bootstrap(BootstrapContext<TrimPattern> $$0) {
      register($$0, SENTRY);
      register($$0, DUNE);
      register($$0, COAST);
      register($$0, WILD);
      register($$0, WARD);
      register($$0, EYE);
      register($$0, VEX);
      register($$0, TIDE);
      register($$0, SNOUT);
      register($$0, RIB);
      register($$0, SPIRE);
      register($$0, WAYFINDER);
      register($$0, SHAPER);
      register($$0, SILENCE);
      register($$0, RAISER);
      register($$0, HOST);
      register($$0, FLOW);
      register($$0, BOLT);
   }

   public static void register(BootstrapContext<TrimPattern> $$0, ResourceKey<TrimPattern> $$1) {
      TrimPattern $$2 = new TrimPattern(defaultAssetId($$1), Component.translatable(Util.makeDescriptionId("trim_pattern", $$1.identifier())), false);
      $$0.register($$1, $$2);
   }

   private static ResourceKey<TrimPattern> registryKey(String $$0) {
      return ResourceKey.create(Registries.TRIM_PATTERN, Identifier.withDefaultNamespace($$0));
   }

   public static Identifier defaultAssetId(ResourceKey<TrimPattern> $$0) {
      return $$0.identifier();
   }
}
