package net.minecraft.world.entity.animal.chicken;

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

public class ChickenVariants {
   public static final ResourceKey<ChickenVariant> TEMPERATE = createKey(TemperatureVariants.TEMPERATE);
   public static final ResourceKey<ChickenVariant> WARM = createKey(TemperatureVariants.WARM);
   public static final ResourceKey<ChickenVariant> COLD = createKey(TemperatureVariants.COLD);
   public static final ResourceKey<ChickenVariant> DEFAULT = TEMPERATE;

   private static ResourceKey<ChickenVariant> createKey(Identifier $$0) {
      return ResourceKey.create(Registries.CHICKEN_VARIANT, $$0);
   }

   public static void bootstrap(BootstrapContext<ChickenVariant> $$0) {
      register($$0, TEMPERATE, ChickenVariant.ModelType.NORMAL, "temperate_chicken", SpawnPrioritySelectors.fallback(0));
      register($$0, WARM, ChickenVariant.ModelType.NORMAL, "warm_chicken", BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
      register($$0, COLD, ChickenVariant.ModelType.COLD, "cold_chicken", BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS);
   }

   private static void register(
      BootstrapContext<ChickenVariant> $$0, ResourceKey<ChickenVariant> $$1, ChickenVariant.ModelType $$2, String $$3, TagKey<Biome> $$4
   ) {
      HolderSet<Biome> $$5 = $$0.lookup(Registries.BIOME).getOrThrow($$4);
      register($$0, $$1, $$2, $$3, SpawnPrioritySelectors.single(new BiomeCheck($$5), 1));
   }

   private static void register(
      BootstrapContext<ChickenVariant> $$0, ResourceKey<ChickenVariant> $$1, ChickenVariant.ModelType $$2, String $$3, SpawnPrioritySelectors $$4
   ) {
      Identifier $$5 = Identifier.withDefaultNamespace("entity/chicken/" + $$3);
      $$0.register($$1, new ChickenVariant(new ModelAndTexture<>($$2, $$5), $$4));
   }
}
