/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public class RegistryDumpReport
implements DataProvider {
    private final PackOutput output;

    public RegistryDumpReport(PackOutput $$0) {
        this.output = $$0;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$0) {
        JsonObject $$12 = new JsonObject();
        BuiltInRegistries.REGISTRY.listElements().forEach($$1 -> $$12.add($$1.key().identifier().toString(), RegistryDumpReport.dumpRegistry((Registry)$$1.value())));
        Path $$2 = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("registries.json");
        return DataProvider.saveStable($$0, (JsonElement)$$12, $$2);
    }

    private static <T> JsonElement dumpRegistry(Registry<T> $$0) {
        JsonObject $$1 = new JsonObject();
        if ($$0 instanceof DefaultedRegistry) {
            Identifier $$22 = ((DefaultedRegistry)$$0).getDefaultKey();
            $$1.addProperty("default", $$22.toString());
        }
        int $$3 = BuiltInRegistries.REGISTRY.getId($$0);
        $$1.addProperty("protocol_id", (Number)$$3);
        JsonObject $$4 = new JsonObject();
        $$0.listElements().forEach($$2 -> {
            Object $$3 = $$2.value();
            int $$4 = $$0.getId($$3);
            JsonObject $$5 = new JsonObject();
            $$5.addProperty("protocol_id", (Number)$$4);
            $$4.add($$2.key().identifier().toString(), (JsonElement)$$5);
        });
        $$1.add("entries", (JsonElement)$$4);
        return $$1;
    }

    @Override
    public final String getName() {
        return "Registry Dump";
    }
}

