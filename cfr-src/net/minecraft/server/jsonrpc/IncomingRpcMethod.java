/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.EncodeJsonRpcException;
import net.minecraft.server.jsonrpc.methods.InvalidParameterJsonRpcException;
import org.jspecify.annotations.Nullable;

public interface IncomingRpcMethod<Params, Result> {
    public MethodInfo<Params, Result> info();

    public Attributes attributes();

    public JsonElement apply(MinecraftApi var1, @Nullable JsonElement var2, ClientInfo var3);

    public static <Result> IncomingRpcMethodBuilder<Void, Result> method(ParameterlessRpcMethodFunction<Result> $$0) {
        return new IncomingRpcMethodBuilder($$0);
    }

    public static <Params, Result> IncomingRpcMethodBuilder<Params, Result> method(RpcMethodFunction<Params, Result> $$0) {
        return new IncomingRpcMethodBuilder<Params, Result>($$0);
    }

    public static <Result> IncomingRpcMethodBuilder<Void, Result> method(Function<MinecraftApi, Result> $$0) {
        return new IncomingRpcMethodBuilder($$0);
    }

    public static class IncomingRpcMethodBuilder<Params, Result> {
        private String description = "";
        private @Nullable ParamInfo<Params> paramInfo;
        private @Nullable ResultInfo<Result> resultInfo;
        private boolean discoverable = true;
        private boolean runOnMainThread = true;
        private @Nullable ParameterlessRpcMethodFunction<Result> parameterlessFunction;
        private @Nullable RpcMethodFunction<Params, Result> parameterFunction;

        public IncomingRpcMethodBuilder(ParameterlessRpcMethodFunction<Result> $$0) {
            this.parameterlessFunction = $$0;
        }

        public IncomingRpcMethodBuilder(RpcMethodFunction<Params, Result> $$0) {
            this.parameterFunction = $$0;
        }

        public IncomingRpcMethodBuilder(Function<MinecraftApi, Result> $$0) {
            this.parameterlessFunction = ($$1, $$2) -> $$0.apply($$1);
        }

        public IncomingRpcMethodBuilder<Params, Result> description(String $$0) {
            this.description = $$0;
            return this;
        }

        public IncomingRpcMethodBuilder<Params, Result> response(String $$0, Schema<Result> $$1) {
            this.resultInfo = new ResultInfo<Result>($$0, $$1.info());
            return this;
        }

        public IncomingRpcMethodBuilder<Params, Result> param(String $$0, Schema<Params> $$1) {
            this.paramInfo = new ParamInfo<Params>($$0, $$1.info());
            return this;
        }

        public IncomingRpcMethodBuilder<Params, Result> undiscoverable() {
            this.discoverable = false;
            return this;
        }

        public IncomingRpcMethodBuilder<Params, Result> notOnMainThread() {
            this.runOnMainThread = false;
            return this;
        }

        public IncomingRpcMethod<Params, Result> build() {
            if (this.resultInfo == null) {
                throw new IllegalStateException("No response defined");
            }
            Attributes $$0 = new Attributes(this.runOnMainThread, this.discoverable);
            MethodInfo<Params, Result> $$1 = new MethodInfo<Params, Result>(this.description, this.paramInfo, this.resultInfo);
            if (this.parameterlessFunction != null) {
                return new ParameterlessMethod<Params, Result>($$1, $$0, this.parameterlessFunction);
            }
            if (this.parameterFunction != null) {
                if (this.paramInfo == null) {
                    throw new IllegalStateException("No param schema defined");
                }
                return new Method<Params, Result>($$1, $$0, this.parameterFunction);
            }
            throw new IllegalStateException("No method defined");
        }

        public IncomingRpcMethod<?, ?> register(Registry<IncomingRpcMethod<?, ?>> $$0, String $$1) {
            return this.register($$0, Identifier.withDefaultNamespace($$1));
        }

        private IncomingRpcMethod<?, ?> register(Registry<IncomingRpcMethod<?, ?>> $$0, Identifier $$1) {
            return Registry.register($$0, $$1, this.build());
        }
    }

    @FunctionalInterface
    public static interface ParameterlessRpcMethodFunction<Result> {
        public Result apply(MinecraftApi var1, ClientInfo var2);
    }

    @FunctionalInterface
    public static interface RpcMethodFunction<Params, Result> {
        public Result apply(MinecraftApi var1, Params var2, ClientInfo var3);
    }

    public record Method<Params, Result>(MethodInfo<Params, Result> info, Attributes attributes, RpcMethodFunction<Params, Result> function) implements IncomingRpcMethod<Params, Result>
    {
        @Override
        public JsonElement apply(MinecraftApi $$0, @Nullable JsonElement $$1, ClientInfo $$2) {
            JsonElement $$7;
            if ($$1 == null || !$$1.isJsonArray() && !$$1.isJsonObject()) {
                throw new InvalidParameterJsonRpcException("Expected params as array or named");
            }
            if (this.info.params().isEmpty()) {
                throw new IllegalArgumentException("Method defined as having parameters without describing them");
            }
            if ($$1.isJsonObject()) {
                String $$3 = this.info.params().get().name();
                JsonElement $$4 = $$1.getAsJsonObject().get($$3);
                if ($$4 == null) {
                    throw new InvalidParameterJsonRpcException(String.format(Locale.ROOT, "Params passed by-name, but expected param [%s] does not exist", $$3));
                }
                JsonElement $$5 = $$4;
            } else {
                JsonArray $$6 = $$1.getAsJsonArray();
                if ($$6.isEmpty() || $$6.size() > 1) {
                    throw new InvalidParameterJsonRpcException("Expected exactly one element in the params array");
                }
                $$7 = $$6.get(0);
            }
            Object $$8 = this.info.params().get().schema().codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)$$7).getOrThrow(InvalidParameterJsonRpcException::new);
            Result $$9 = this.function.apply($$0, $$8, $$2);
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("No result codec defined");
            }
            return (JsonElement)this.info.result().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, $$9).getOrThrow(EncodeJsonRpcException::new);
        }
    }

    public record ParameterlessMethod<Params, Result>(MethodInfo<Params, Result> info, Attributes attributes, ParameterlessRpcMethodFunction<Result> supplier) implements IncomingRpcMethod<Params, Result>
    {
        @Override
        public JsonElement apply(MinecraftApi $$0, @Nullable JsonElement $$1, ClientInfo $$2) {
            if (!($$1 == null || $$1.isJsonArray() && $$1.getAsJsonArray().isEmpty())) {
                throw new InvalidParameterJsonRpcException("Expected no params, or an empty array");
            }
            if (this.info.params().isPresent()) {
                throw new IllegalArgumentException("Parameterless method unexpectedly has parameter description");
            }
            Result $$3 = this.supplier.apply($$0, $$2);
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("No result codec defined");
            }
            return (JsonElement)this.info.result().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, $$3).getOrThrow(InvalidParameterJsonRpcException::new);
        }
    }

    public record Attributes(boolean runOnMainThread, boolean discoverable) {
    }
}

