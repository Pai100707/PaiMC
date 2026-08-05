/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class ReloadableServerRegistries {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RegistrationInfo DEFAULT_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());

    public static CompletableFuture<LoadResult> reload(LayeredRegistryAccess<RegistryLayer> $$0, List<Registry.PendingTags<?>> $$1, ResourceManager $$22, Executor $$32) {
        List<HolderLookup.RegistryLookup<?>> $$4 = TagLoader.buildUpdatedLookups($$0.getAccessForLoading(RegistryLayer.RELOADABLE), $$1);
        HolderLookup.Provider $$5 = HolderLookup.Provider.create($$4.stream());
        RegistryOps $$6 = $$5.createSerializationContext(JsonOps.INSTANCE);
        List<CompletableFuture> $$7 = LootDataType.values().map($$3 -> ReloadableServerRegistries.scheduleRegistryLoad($$3, $$6, $$22, $$32)).toList();
        CompletableFuture $$8 = Util.sequence($$7);
        return $$8.thenApplyAsync($$2 -> ReloadableServerRegistries.createAndValidateFullContext($$0, $$5, $$2), $$32);
    }

    private static <T> CompletableFuture<WritableRegistry<?>> scheduleRegistryLoad(LootDataType<T> $$0, RegistryOps<JsonElement> $$1, ResourceManager $$2, Executor $$3) {
        return CompletableFuture.supplyAsync(() -> {
            MappedRegistry $$32 = new MappedRegistry($$0.registryKey(), Lifecycle.experimental());
            HashMap<Identifier, Object> $$4 = new HashMap<Identifier, Object>();
            SimpleJsonResourceReloadListener.scanDirectory($$2, $$0.registryKey(), (DynamicOps<JsonElement>)$$1, $$0.codec(), $$4);
            $$4.forEach(($$2, $$3) -> $$32.register(ResourceKey.create($$0.registryKey(), $$2), $$3, DEFAULT_REGISTRATION_INFO));
            TagLoader.loadTagsForRegistry($$2, $$32);
            return $$32;
        }, $$3);
    }

    private static LoadResult createAndValidateFullContext(LayeredRegistryAccess<RegistryLayer> $$0, HolderLookup.Provider $$1, List<WritableRegistry<?>> $$2) {
        LayeredRegistryAccess<RegistryLayer> $$3 = ReloadableServerRegistries.createUpdatedRegistries($$0, $$2);
        HolderLookup.Provider $$4 = ReloadableServerRegistries.concatenateLookups($$1, $$3.getLayer(RegistryLayer.RELOADABLE));
        ReloadableServerRegistries.validateLootRegistries($$4);
        return new LoadResult($$3, $$4);
    }

    private static HolderLookup.Provider concatenateLookups(HolderLookup.Provider $$0, HolderLookup.Provider $$1) {
        return HolderLookup.Provider.create(Stream.concat($$0.listRegistries(), $$1.listRegistries()));
    }

    private static void validateLootRegistries(HolderLookup.Provider $$02) {
        ProblemReporter.Collector $$12 = new ProblemReporter.Collector();
        ValidationContext $$22 = new ValidationContext($$12, LootContextParamSets.ALL_PARAMS, $$02);
        LootDataType.values().forEach($$2 -> ReloadableServerRegistries.validateRegistry($$22, $$2, $$02));
        $$12.forEach(($$0, $$1) -> LOGGER.warn("Found loot table element validation problem in {}: {}", $$0, (Object)$$1.description()));
    }

    private static LayeredRegistryAccess<RegistryLayer> createUpdatedRegistries(LayeredRegistryAccess<RegistryLayer> $$0, List<WritableRegistry<?>> $$1) {
        return $$0.replaceFrom(RegistryLayer.RELOADABLE, new RegistryAccess.ImmutableRegistryAccess($$1).freeze());
    }

    private static <T> void validateRegistry(ValidationContext $$0, LootDataType<T> $$1, HolderLookup.Provider $$22) {
        HolderGetter $$3 = $$22.lookupOrThrow($$1.registryKey());
        $$3.listElements().forEach($$2 -> $$1.runValidation($$0, $$2.key(), $$2.value()));
    }

    public record LoadResult(LayeredRegistryAccess<RegistryLayer> layers, HolderLookup.Provider lookupWithUpdatedTags) {
    }

    public static class Holder {
        private final HolderLookup.Provider registries;

        public Holder(HolderLookup.Provider $$0) {
            this.registries = $$0;
        }

        public HolderLookup.Provider lookup() {
            return this.registries;
        }

        public LootTable getLootTable(ResourceKey<LootTable> $$0) {
            return this.registries.lookup(Registries.LOOT_TABLE).flatMap($$1 -> $$1.get($$0)).map(net.minecraft.core.Holder::value).orElse(LootTable.EMPTY);
        }
    }
}

