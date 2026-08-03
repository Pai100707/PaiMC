package net.minecraft.data.registries;

import com.mojang.datafixers.DataFixUtils;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.Cloner.Factory;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.RegistrySetBuilder.PatchedRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RegistryPatchGenerator {
   public static CompletableFuture<PatchedRegistries> createLookup(CompletableFuture<Provider> $$0, RegistrySetBuilder $$1) {
      return $$0.thenApply(
         $$1x -> {
            Frozen $$2 = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
            Factory $$3 = new Factory();
            RegistryDataLoader.WORLDGEN_REGISTRIES.forEach($$1xx -> $$1xx.runWithArguments($$3::addCodec));
            PatchedRegistries $$4 = $$1.buildPatch($$2, $$1x, $$3);
            Provider $$5 = $$4.full();
            Optional<? extends RegistryLookup<Biome>> $$6 = $$5.lookup(Registries.BIOME);
            Optional<? extends RegistryLookup<PlacedFeature>> $$7 = $$5.lookup(Registries.PLACED_FEATURE);
            if ($$6.isPresent() || $$7.isPresent()) {
               VanillaRegistries.validateThatAllBiomeFeaturesHaveBiomeFilter(
                  (HolderGetter<PlacedFeature>)DataFixUtils.orElseGet($$7, () -> $$1x.lookupOrThrow(Registries.PLACED_FEATURE)),
                  (HolderLookup<Biome>)DataFixUtils.orElseGet($$6, () -> $$1x.lookupOrThrow(Registries.BIOME))
               );
            }

            return $$4;
         }
      );
   }
}
