/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;

public class ItemListReport
implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public ItemListReport(PackOutput $$0, CompletableFuture<HolderLookup.Provider> $$1) {
        this.output = $$0;
        this.registries = $$1;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$0) {
        Path $$1 = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("items.json");
        return this.registries.thenCompose($$22 -> {
            JsonObject $$3 = new JsonObject();
            RegistryOps $$4 = $$22.createSerializationContext(JsonOps.INSTANCE);
            $$22.lookupOrThrow(Registries.ITEM).listElements().forEach($$2 -> {
                JsonObject $$3 = new JsonObject();
                $$3.add("components", (JsonElement)DataComponentMap.CODEC.encodeStart((DynamicOps)$$4, (Object)((Item)$$2.value()).components()).getOrThrow($$0 -> new IllegalStateException("Failed to encode components: " + $$0)));
                $$3.add($$2.getRegisteredName(), (JsonElement)$$3);
            });
            return DataProvider.saveStable($$0, (JsonElement)$$3, $$1);
        });
    }

    @Override
    public final String getName() {
        return "Item List";
    }
}

