/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Encoder
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.data.registries;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

public class RegistriesDatapackGenerator
implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public RegistriesDatapackGenerator(PackOutput $$0, CompletableFuture<HolderLookup.Provider> $$1) {
        this.registries = $$1;
        this.output = $$0;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$0) {
        return this.registries.thenCompose($$1 -> {
            RegistryOps $$2 = $$1.createSerializationContext(JsonOps.INSTANCE);
            return CompletableFuture.allOf((CompletableFuture[])RegistryDataLoader.WORLDGEN_REGISTRIES.stream().flatMap($$3 -> this.dumpRegistryCap($$0, (HolderLookup.Provider)$$1, $$2, (RegistryDataLoader.RegistryData)$$3).stream()).toArray(CompletableFuture[]::new));
        });
    }

    private <T> Optional<CompletableFuture<?>> dumpRegistryCap(CachedOutput $$0, HolderLookup.Provider $$1, DynamicOps<JsonElement> $$2, RegistryDataLoader.RegistryData<T> $$3) {
        ResourceKey $$4 = $$3.key();
        return $$1.lookup($$4).map($$42 -> {
            PackOutput.PathProvider $$5 = this.output.createRegistryElementsPathProvider($$4);
            return CompletableFuture.allOf((CompletableFuture[])$$42.listElements().map($$4 -> RegistriesDatapackGenerator.dumpValue($$5.json($$4.key().identifier()), $$0, $$2, $$3.elementCodec(), $$4.value())).toArray(CompletableFuture[]::new));
        });
    }

    private static <E> CompletableFuture<?> dumpValue(Path $$0, CachedOutput $$12, DynamicOps<JsonElement> $$22, Encoder<E> $$3, E $$4) {
        return (CompletableFuture)$$3.encodeStart($$22, $$4).mapOrElse($$2 -> DataProvider.saveStable($$12, $$2, $$0), $$1 -> CompletableFuture.failedFuture(new IllegalStateException("Couldn't generate file '" + String.valueOf($$0) + "': " + $$1.message())));
    }

    @Override
    public final String getName() {
        return "Registries";
    }
}

