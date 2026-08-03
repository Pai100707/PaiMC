package net.minecraft.world.entity.animal.pig;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.TemperatureVariants;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.biome.Biome;

public class PigVariants {
   public static final ResourceKey<PigVariant> TEMPERATE = createKey(TemperatureVariants.TEMPERATE);
   public static final ResourceKey<PigVariant> WARM = createKey(TemperatureVariants.WARM);
   public static final ResourceKey<PigVariant> COLD = createKey(TemperatureVariants.COLD);
   public static final ResourceKey<PigVariant> DEFAULT = TEMPERATE;

   private static ResourceKey<PigVariant> createKey(Identifier $$0) {
      return ResourceKey.create(Registries.PIG_VARIANT, $$0);
   }

   public static void bootstrap(BootstrapContext<PigVariant> $$0) {
      register($$0, TEMPERATE, PigVariant.ModelType.NORMAL, "temperate_pig", SpawnPrioritySelectors.fallback(0));
      register($$0, WARM, PigVariant.ModelType.NORMAL, "warm_pig", BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
      register($$0, COLD, PigVariant.ModelType.COLD, "cold_pig", BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS);
   }

   private static void register(BootstrapContext<PigVariant> $$0, ResourceKey<PigVariant> $$1, PigVariant.ModelType $$2, String $$3, TagKey<Biome> $$4) {
      HolderSet<Biome> $$5 = $$0.lookup(Registries.BIOME).getOrThrow($$4);
      register($$0, $$1, $$2, $$3, SpawnPrioritySelectors.single(new BiomeCheck($$5), 1));
   }

   private static void register(BootstrapContext<PigVariant> $$0, ResourceKey<PigVariant> $$1, PigVariant.ModelType $$2, String $$3, SpawnPrioritySelectors $$4) {
      Identifier $$5 = Identifier.withDefaultNamespace("entity/pig/" + $$3);
      $$0.register($$1, new PigVariant(new ModelAndTexture<>($$2, $$5), $$4));
   }
}
