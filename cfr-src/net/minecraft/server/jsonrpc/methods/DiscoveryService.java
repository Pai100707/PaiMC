/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.jsonrpc.IncomingRpcMethod;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.api.SchemaComponent;

public class DiscoveryService {
    public static DiscoverResponse discover(List<SchemaComponent<?>> $$0) {
        ArrayList $$12 = new ArrayList(BuiltInRegistries.INCOMING_RPC_METHOD.size() + BuiltInRegistries.OUTGOING_RPC_METHOD.size());
        BuiltInRegistries.INCOMING_RPC_METHOD.listElements().forEach($$1 -> {
            if (((IncomingRpcMethod)$$1.value()).attributes().discoverable()) {
                $$12.add(((IncomingRpcMethod)$$1.value()).info().named($$1.key().identifier()));
            }
        });
        BuiltInRegistries.OUTGOING_RPC_METHOD.listElements().forEach($$1 -> {
            if (((OutgoingRpcMethod)$$1.value()).attributes().discoverable()) {
                $$12.add(((OutgoingRpcMethod)$$1.value()).info().named($$1.key().identifier()));
            }
        });
        HashMap $$2 = new HashMap();
        for (SchemaComponent<?> $$3 : $$0) {
            $$2.put($$3.name(), $$3.schema().info());
        }
        DiscoverInfo $$4 = new DiscoverInfo("Minecraft Server JSON-RPC", "2.0.0");
        return new DiscoverResponse("1.3.2", $$4, $$12, new DiscoverComponents($$2));
    }

    public record DiscoverInfo(String title, String version) {
        public static final MapCodec<DiscoverInfo> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.STRING.fieldOf("title").forGetter(DiscoverInfo::title), (App)Codec.STRING.fieldOf("version").forGetter(DiscoverInfo::version)).apply((Applicative)$$0, DiscoverInfo::new));
    }

    public record DiscoverResponse(String jsonRpcProtocolVersion, DiscoverInfo discoverInfo, List<MethodInfo.Named<?, ?>> methods, DiscoverComponents components) {
        public static final MapCodec<DiscoverResponse> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.STRING.fieldOf("openrpc").forGetter(DiscoverResponse::jsonRpcProtocolVersion), (App)DiscoverInfo.CODEC.codec().fieldOf("info").forGetter(DiscoverResponse::discoverInfo), (App)Codec.list(MethodInfo.Named.CODEC).fieldOf("methods").forGetter(DiscoverResponse::methods), (App)DiscoverComponents.CODEC.codec().fieldOf("components").forGetter(DiscoverResponse::components)).apply((Applicative)$$0, DiscoverResponse::new));
    }

    public record DiscoverComponents(Map<String, Schema<?>> schemas) {
        public static final MapCodec<DiscoverComponents> CODEC = DiscoverComponents.typedSchema();

        private static MapCodec<DiscoverComponents> typedSchema() {
            return RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.unboundedMap((Codec)Codec.STRING, Schema.CODEC).fieldOf("schemas").forGetter(DiscoverComponents::schemas)).apply((Applicative)$$0, DiscoverComponents::new));
        }
    }
}

