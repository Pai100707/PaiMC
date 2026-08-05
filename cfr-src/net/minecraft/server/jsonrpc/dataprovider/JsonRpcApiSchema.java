/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.server.jsonrpc.dataprovider;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.methods.DiscoveryService;

public class JsonRpcApiSchema
implements DataProvider {
    private final Path path;

    public JsonRpcApiSchema(PackOutput $$0) {
        this.path = $$0.getOutputFolder(PackOutput.Target.REPORTS).resolve("json-rpc-api-schema.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$0) {
        DiscoveryService.DiscoverResponse $$1 = DiscoveryService.discover(Schema.getSchemaRegistry());
        return DataProvider.saveStable($$0, (JsonElement)DiscoveryService.DiscoverResponse.CODEC.codec().encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)$$1).getOrThrow(), this.path);
    }

    @Override
    public String getName() {
        return "Json RPC API schema";
    }
}

