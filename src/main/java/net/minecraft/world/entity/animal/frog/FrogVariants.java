package net.minecraft.world.entity.animal.frog;

import net.minecraft.core.HolderSet;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.TemperatureVariants;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.biome.Biome;

public interface FrogVariants {
   ResourceKey<FrogVariant> TEMPERATE = createKey(TemperatureVariants.TEMPERATE);
   ResourceKey<FrogVariant> WARM = createKey(TemperatureVariants.WARM);
   ResourceKey<FrogVariant> COLD = createKey(TemperatureVariants.COLD);

   private static ResourceKey<FrogVariant> createKey(Identifier $$0) {
      return ResourceKey.create(Registries.FROG_VARIANT, $$0);
   }

   static void bootstrap(BootstrapContext<FrogVariant> $$0) {
      register($$0, TEMPERATE, "entity/frog/temperate_frog", SpawnPrioritySelectors.fallback(0));
      register($$0, WARM, "entity/frog/warm_frog", BiomeTags.SPAWNS_WARM_VARIANT_FROGS);
      register($$0, COLD, "entity/frog/cold_frog", BiomeTags.SPAWNS_COLD_VARIANT_FROGS);
   }

   private static void register(BootstrapContext<FrogVariant> $$0, ResourceKey<FrogVariant> $$1, String $$2, TagKey<Biome> $$3) {
      HolderSet<Biome> $$4 = $$0.lookup(Registries.BIOME).getOrThrow($$3);
      register($$0, $$1, $$2, SpawnPrioritySelectors.single(new BiomeCheck($$4), 1));
   }

   private static void register(BootstrapContext<FrogVariant> $$0, ResourceKey<FrogVariant> $$1, String $$2, SpawnPrioritySelectors $$3) {
      $$0.register($$1, new FrogVariant(new ResourceTexture(Identifier.withDefaultNamespace($$2)), $$3));
   }
}
