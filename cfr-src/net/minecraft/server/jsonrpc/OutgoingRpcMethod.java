/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import net.minecraft.server.jsonrpc.api.Schema;
import org.jspecify.annotations.Nullable;

public interface OutgoingRpcMethod<Params, Result> {
    public static final String NOTIFICATION_PREFIX = "notification/";

    public MethodInfo<Params, Result> info();

    public Attributes attributes();

    default public @Nullable JsonElement encodeParams(Params $$0) {
        return null;
    }

    default public @Nullable Result decodeResult(JsonElement $$0) {
        return null;
    }

    public static OutgoingRpcMethodBuilder<Void, Void> notification() {
        return new OutgoingRpcMethodBuilder<Void, Void>(ParmeterlessNotification::new);
    }

    public static <Params> OutgoingRpcMethodBuilder<Params, Void> notificationWithParams() {
        return new OutgoingRpcMethodBuilder(Notification::new);
    }

    public static <Result> OutgoingRpcMethodBuilder<Void, Result> request() {
        return new OutgoingRpcMethodBuilder(ParameterlessMethod::new);
    }

    public static <Params, Result> OutgoingRpcMethodBuilder<Params, Result> requestWithParams() {
        return new OutgoingRpcMethodBuilder(Method::new);
    }

    public static class OutgoingRpcMethodBuilder<Params, Result> {
        public static final Attributes DEFAULT_ATTRIBUTES = new Attributes(true);
        private final Factory<Params, Result> method;
        private String description = "";
        private @Nullable ParamInfo<Params> paramInfo;
        private @Nullable ResultInfo<Result> resultInfo;

        public OutgoingRpcMethodBuilder(Factory<Params, Result> $$0) {
            this.method = $$0;
        }

        public OutgoingRpcMethodBuilder<Params, Result> description(String $$0) {
            this.description = $$0;
            return this;
        }

        public OutgoingRpcMethodBuilder<Params, Result> response(String $$0, Schema<Result> $$1) {
            this.resultInfo = new ResultInfo<Result>($$0, $$1);
            return this;
        }

        public OutgoingRpcMethodBuilder<Params, Result> param(String $$0, Schema<Params> $$1) {
            this.paramInfo = new ParamInfo<Params>($$0, $$1);
            return this;
        }

        private OutgoingRpcMethod<Params, Result> build() {
            MethodInfo<Params, Result> $$0 = new MethodInfo<Params, Result>(this.description, this.paramInfo, this.resultInfo);
            return this.method.create($$0, DEFAULT_ATTRIBUTES);
        }

        public Holder.Reference<OutgoingRpcMethod<Params, Result>> register(String $$0) {
            return this.register(Identifier.withDefaultNamespace(OutgoingRpcMethod.NOTIFICATION_PREFIX + $$0));
        }

        private Holder.Reference<OutgoingRpcMethod<Params, Result>> register(Identifier $$0) {
            return Registry.registerForHolder(BuiltInRegistries.OUTGOING_RPC_METHOD, $$0, this.build());
        }
    }

    @FunctionalInterface
    public static interface Factory<Params, Result> {
        public OutgoingRpcMethod<Params, Result> create(MethodInfo<Params, Result> var1, Attributes var2);
    }

    public record Method<Params, Result>(MethodInfo<Params, Result> info, Attributes attributes) implements OutgoingRpcMethod<Params, Result>
    {
        @Override
        public @Nullable JsonElement encodeParams(Params $$0) {
            if (this.info.params().isEmpty()) {
                throw new IllegalStateException("Method defined as having no parameters");
            }
            return (JsonElement)this.info.params().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, $$0).getOrThrow();
        }

        @Override
        public Result decodeResult(JsonElement $$0) {
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("Method defined as having no result");
            }
            return (Result)this.info.result().get().schema().codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)$$0).getOrThrow();
        }
    }

    public record ParameterlessMethod<Result>(MethodInfo<Void, Result> info, Attributes attributes) implements OutgoingRpcMethod<Void, Result>
    {
        @Override
        public Result decodeResult(JsonElement $$0) {
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("Method defined as having no result");
            }
            return (Result)this.info.result().get().schema().codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)$$0).getOrThrow();
        }
    }

    public record Notification<Params>(MethodInfo<Params, Void> info, Attributes attributes) implements OutgoingRpcMethod<Params, Void>
    {
        @Override
        public @Nullable JsonElement encodeParams(Params $$0) {
            if (this.info.params().isEmpty()) {
                throw new IllegalStateException("Method defined as having no parameters");
            }
            return (JsonElement)this.info.params().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, $$0).getOrThrow();
        }
    }

    public record ParmeterlessNotification(MethodInfo<Void, Void> info, Attributes attributes) implements OutgoingRpcMethod<Void, Void>
    {
    }

    public record Attributes(boolean discoverable) {
    }
}

