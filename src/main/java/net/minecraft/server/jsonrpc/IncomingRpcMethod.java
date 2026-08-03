package net.minecraft.server.jsonrpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
   MethodInfo<Params, Result> info();

   IncomingRpcMethod.Attributes attributes();

   JsonElement apply(MinecraftApi var1, @Nullable JsonElement var2, ClientInfo var3);

   static <Result> IncomingRpcMethod.IncomingRpcMethodBuilder<Void, Result> method(IncomingRpcMethod.ParameterlessRpcMethodFunction<Result> $$0) {
      return new IncomingRpcMethod.IncomingRpcMethodBuilder<>($$0);
   }

   static <Params, Result> IncomingRpcMethod.IncomingRpcMethodBuilder<Params, Result> method(IncomingRpcMethod.RpcMethodFunction<Params, Result> $$0) {
      return new IncomingRpcMethod.IncomingRpcMethodBuilder<>($$0);
   }

   static <Result> IncomingRpcMethod.IncomingRpcMethodBuilder<Void, Result> method(Function<MinecraftApi, Result> $$0) {
      return new IncomingRpcMethod.IncomingRpcMethodBuilder<>($$0);
   }

   public record Attributes(boolean runOnMainThread, boolean discoverable) {
   }

   public static class IncomingRpcMethodBuilder<Params, Result> {
      private String description = "";
      @Nullable
      private ParamInfo<Params> paramInfo;
      @Nullable
      private ResultInfo<Result> resultInfo;
      private boolean discoverable = true;
      private boolean runOnMainThread = true;
      @Nullable
      private IncomingRpcMethod.ParameterlessRpcMethodFunction<Result> parameterlessFunction;
      @Nullable
      private IncomingRpcMethod.RpcMethodFunction<Params, Result> parameterFunction;

      public IncomingRpcMethodBuilder(IncomingRpcMethod.ParameterlessRpcMethodFunction<Result> $$0) {
         this.parameterlessFunction = $$0;
      }

      public IncomingRpcMethodBuilder(IncomingRpcMethod.RpcMethodFunction<Params, Result> $$0) {
         this.parameterFunction = $$0;
      }

      public IncomingRpcMethodBuilder(Function<MinecraftApi, Result> $$0) {
         this.parameterlessFunction = ($$1, $$2) -> $$0.apply($$1);
      }

      public IncomingRpcMethod.IncomingRpcMethodBuilder<Params, Result> description(String $$0) {
         this.description = $$0;
         return this;
      }

      public IncomingRpcMethod.IncomingRpcMethodBuilder<Params, Result> response(String $$0, Schema<Result> $$1) {
         this.resultInfo = new ResultInfo<>($$0, $$1.info());
         return this;
      }

      public IncomingRpcMethod.IncomingRpcMethodBuilder<Params, Result> param(String $$0, Schema<Params> $$1) {
         this.paramInfo = new ParamInfo<>($$0, $$1.info());
         return this;
      }

      public IncomingRpcMethod.IncomingRpcMethodBuilder<Params, Result> undiscoverable() {
         this.discoverable = false;
         return this;
      }

      public IncomingRpcMethod.IncomingRpcMethodBuilder<Params, Result> notOnMainThread() {
         this.runOnMainThread = false;
         return this;
      }

      public IncomingRpcMethod<Params, Result> build() {
         if (this.resultInfo == null) {
            throw new IllegalStateException("No response defined");
         } else {
            IncomingRpcMethod.Attributes $$0 = new IncomingRpcMethod.Attributes(this.runOnMainThread, this.discoverable);
            MethodInfo<Params, Result> $$1 = new MethodInfo<>(this.description, this.paramInfo, this.resultInfo);
            if (this.parameterlessFunction != null) {
               return new IncomingRpcMethod.ParameterlessMethod<>($$1, $$0, this.parameterlessFunction);
            } else if (this.parameterFunction != null) {
               if (this.paramInfo == null) {
                  throw new IllegalStateException("No param schema defined");
               } else {
                  return new IncomingRpcMethod.Method<>($$1, $$0, this.parameterFunction);
               }
            } else {
               throw new IllegalStateException("No method defined");
            }
         }
      }

      public IncomingRpcMethod<?, ?> register(Registry<IncomingRpcMethod<?, ?>> $$0, String $$1) {
         return this.register($$0, Identifier.withDefaultNamespace($$1));
      }

      private IncomingRpcMethod<?, ?> register(Registry<IncomingRpcMethod<?, ?>> $$0, Identifier $$1) {
         return (IncomingRpcMethod<?, ?>)Registry.register($$0, $$1, this.build());
      }
   }

   public record Method<Params, Result>(
      MethodInfo<Params, Result> info, IncomingRpcMethod.Attributes attributes, IncomingRpcMethod.RpcMethodFunction<Params, Result> function
   ) implements IncomingRpcMethod<Params, Result> {
      @Override
      public JsonElement apply(MinecraftApi $$0, @Nullable JsonElement $$1, ClientInfo $$2) {
         if ($$1 != null && ($$1.isJsonArray() || $$1.isJsonObject())) {
            if (this.info.params().isEmpty()) {
               throw new IllegalArgumentException("Method defined as having parameters without describing them");
            } else {
               JsonElement $$5;
               if ($$1.isJsonObject()) {
                  String $$3 = this.info.params().get().name();
                  JsonElement $$4 = $$1.getAsJsonObject().get($$3);
                  if ($$4 == null) {
                     throw new InvalidParameterJsonRpcException(
                        String.format(Locale.ROOT, "Params passed by-name, but expected param [%s] does not exist", $$3)
                     );
                  }

                  $$5 = $$4;
               } else {
                  JsonArray $$6 = $$1.getAsJsonArray();
                  if ($$6.isEmpty() || $$6.size() > 1) {
                     throw new InvalidParameterJsonRpcException("Expected exactly one element in the params array");
                  }

                  $$5 = $$6.get(0);
               }

               Params $$8 = (Params)this.info.params().get().schema().codec().parse(JsonOps.INSTANCE, $$5).getOrThrow(InvalidParameterJsonRpcException::new);
               Result $$9 = this.function.apply($$0, $$8, $$2);
               if (this.info.result().isEmpty()) {
                  throw new IllegalStateException("No result codec defined");
               } else {
                  return (JsonElement)this.info.result().get().schema().codec().encodeStart(JsonOps.INSTANCE, $$9).getOrThrow(EncodeJsonRpcException::new);
               }
            }
         } else {
            throw new InvalidParameterJsonRpcException("Expected params as array or named");
         }
      }
   }

   public record ParameterlessMethod<Params, Result>(
      MethodInfo<Params, Result> info, IncomingRpcMethod.Attributes attributes, IncomingRpcMethod.ParameterlessRpcMethodFunction<Result> supplier
   ) implements IncomingRpcMethod<Params, Result> {
      @Override
      public JsonElement apply(MinecraftApi $$0, @Nullable JsonElement $$1, ClientInfo $$2) {
         if ($$1 == null || $$1.isJsonArray() && $$1.getAsJsonArray().isEmpty()) {
            if (this.info.params().isPresent()) {
               throw new IllegalArgumentException("Parameterless method unexpectedly has parameter description");
            } else {
               Result $$3 = this.supplier.apply($$0, $$2);
               if (this.info.result().isEmpty()) {
                  throw new IllegalStateException("No result codec defined");
               } else {
                  return (JsonElement)this.info
                     .result()
                     .get()
                     .schema()
                     .codec()
                     .encodeStart(JsonOps.INSTANCE, $$3)
                     .getOrThrow(InvalidParameterJsonRpcException::new);
               }
            }
         } else {
            throw new InvalidParameterJsonRpcException("Expected no params, or an empty array");
         }
      }
   }

   @FunctionalInterface
   public interface ParameterlessRpcMethodFunction<Result> {
      Result apply(MinecraftApi var1, ClientInfo var2);
   }

   @FunctionalInterface
   public interface RpcMethodFunction<Params, Result> {
      Result apply(MinecraftApi var1, Params var2, ClientInfo var3);
   }
}
