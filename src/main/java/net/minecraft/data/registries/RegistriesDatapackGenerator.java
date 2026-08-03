package net.minecraft.data.registries;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryDataLoader.RegistryData;

public class RegistriesDatapackGenerator implements net.minecraft.data.DataProvider {
   private final net.minecraft.data.PackOutput output;
   private final CompletableFuture<Provider> registries;

   public RegistriesDatapackGenerator(net.minecraft.data.PackOutput $$0, CompletableFuture<Provider> $$1) {
      this.registries = $$1;
      this.output = $$0;
   }

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      return this.registries
         .thenCompose(
            $$1 -> {
               DynamicOps<JsonElement> $$2 = $$1.createSerializationContext(JsonOps.INSTANCE);
               return CompletableFuture.allOf(
                  RegistryDataLoader.WORLDGEN_REGISTRIES
                     .stream()
                     .flatMap($$3 -> this.dumpRegistryCap($$0, $$1, $$2, $$3).stream())
                     .toArray(CompletableFuture[]::new)
               );
            }
         );
   }

   private <T> Optional<CompletableFuture<?>> dumpRegistryCap(
      net.minecraft.data.CachedOutput $$0, Provider $$1, DynamicOps<JsonElement> $$2, RegistryData<T> $$3
   ) {
      ResourceKey<? extends Registry<T>> $$4 = $$3.key();
      return $$1.lookup($$4)
         .map(
            $$4x -> {
               net.minecraft.data.PackOutput.PathProvider $$5 = this.output.createRegistryElementsPathProvider($$4);
               return CompletableFuture.allOf(
                  $$4x.listElements()
                     .map($$4xx -> dumpValue($$5.json($$4xx.key().identifier()), $$0, $$2, $$3.elementCodec(), $$4xx.value()))
                     .toArray(CompletableFuture[]::new)
               );
            }
         );
   }

   private static <E> CompletableFuture<?> dumpValue(Path $$0, net.minecraft.data.CachedOutput $$1, DynamicOps<JsonElement> $$2, Encoder<E> $$3, E $$4) {
      return (CompletableFuture<?>)$$3.encodeStart($$2, $$4)
         .mapOrElse(
            $$2x -> net.minecraft.data.DataProvider.saveStable($$1, $$2x, $$0),
            $$1x -> CompletableFuture.failedFuture(new IllegalStateException("Couldn't generate file '" + $$0 + "': " + $$1x.message()))
         );
   }

   @Override
   public final String getName() {
      return "Registries";
   }
}
