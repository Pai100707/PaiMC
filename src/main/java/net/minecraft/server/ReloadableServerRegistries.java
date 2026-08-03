package net.minecraft.server;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry.PendingTags;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.RegistryAccess.ImmutableRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.Collector;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class ReloadableServerRegistries {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final RegistrationInfo DEFAULT_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());

   public static CompletableFuture<net.minecraft.server.ReloadableServerRegistries.LoadResult> reload(
      LayeredRegistryAccess<net.minecraft.server.RegistryLayer> $$0, List<PendingTags<?>> $$1, ResourceManager $$2, Executor $$3
   ) {
      List<RegistryLookup<?>> $$4 = TagLoader.buildUpdatedLookups($$0.getAccessForLoading(net.minecraft.server.RegistryLayer.RELOADABLE), $$1);
      Provider $$5 = Provider.create($$4.stream());
      RegistryOps<JsonElement> $$6 = $$5.createSerializationContext(JsonOps.INSTANCE);
      List<CompletableFuture<WritableRegistry<?>>> $$7 = LootDataType.values().map($$3x -> scheduleRegistryLoad($$3x, $$6, $$2, $$3)).toList();
      CompletableFuture<List<WritableRegistry<?>>> $$8 = Util.sequence($$7);
      return $$8.thenApplyAsync($$2x -> createAndValidateFullContext($$0, $$5, $$2x), $$3);
   }

   private static <T> CompletableFuture<WritableRegistry<?>> scheduleRegistryLoad(
      LootDataType<T> $$0, RegistryOps<JsonElement> $$1, ResourceManager $$2, Executor $$3
   ) {
      return CompletableFuture.supplyAsync(() -> {
         WritableRegistry<T> $$3x = new MappedRegistry($$0.registryKey(), Lifecycle.experimental());
         Map<Identifier, T> $$4 = new HashMap<>();
         SimpleJsonResourceReloadListener.scanDirectory($$2, $$0.registryKey(), $$1, $$0.codec(), $$4);
         $$4.forEach(($$2xx, $$3xx) -> $$3x.register(ResourceKey.create($$0.registryKey(), $$2xx), $$3xx, DEFAULT_REGISTRATION_INFO));
         TagLoader.loadTagsForRegistry($$2, $$3x);
         return $$3x;
      }, $$3);
   }

   private static net.minecraft.server.ReloadableServerRegistries.LoadResult createAndValidateFullContext(
      LayeredRegistryAccess<net.minecraft.server.RegistryLayer> $$0, Provider $$1, List<WritableRegistry<?>> $$2
   ) {
      LayeredRegistryAccess<net.minecraft.server.RegistryLayer> $$3 = createUpdatedRegistries($$0, $$2);
      Provider $$4 = concatenateLookups($$1, $$3.getLayer(net.minecraft.server.RegistryLayer.RELOADABLE));
      validateLootRegistries($$4);
      return new net.minecraft.server.ReloadableServerRegistries.LoadResult($$3, $$4);
   }

   private static Provider concatenateLookups(Provider $$0, Provider $$1) {
      return Provider.create(Stream.concat($$0.listRegistries(), $$1.listRegistries()));
   }

   private static void validateLootRegistries(Provider $$0) {
      Collector $$1 = new Collector();
      ValidationContext $$2 = new ValidationContext($$1, LootContextParamSets.ALL_PARAMS, $$0);
      LootDataType.values().forEach($$2x -> validateRegistry($$2, $$2x, $$0));
      $$1.forEach(($$0x, $$1x) -> LOGGER.warn("Found loot table element validation problem in {}: {}", $$0x, $$1x.description()));
   }

   private static LayeredRegistryAccess<net.minecraft.server.RegistryLayer> createUpdatedRegistries(
      LayeredRegistryAccess<net.minecraft.server.RegistryLayer> $$0, List<WritableRegistry<?>> $$1
   ) {
      return $$0.replaceFrom(net.minecraft.server.RegistryLayer.RELOADABLE, new Frozen[]{new ImmutableRegistryAccess($$1).freeze()});
   }

   private static <T> void validateRegistry(ValidationContext $$0, LootDataType<T> $$1, Provider $$2) {
      HolderLookup<T> $$3 = $$2.lookupOrThrow($$1.registryKey());
      $$3.listElements().forEach($$2x -> $$1.runValidation($$0, $$2x.key(), $$2x.value()));
   }

   public static class Holder {
      private final Provider registries;

      public Holder(Provider $$0) {
         this.registries = $$0;
      }

      public Provider lookup() {
         return this.registries;
      }

      public LootTable getLootTable(ResourceKey<LootTable> $$0) {
         return this.registries
            .lookup(Registries.LOOT_TABLE)
            .flatMap($$1 -> $$1.get($$0))
            .<LootTable>map(net.minecraft.core.Holder::value)
            .orElse(LootTable.EMPTY);
      }
   }

   public record LoadResult(LayeredRegistryAccess<net.minecraft.server.RegistryLayer> layers, Provider lookupWithUpdatedTags) {
   }
}
