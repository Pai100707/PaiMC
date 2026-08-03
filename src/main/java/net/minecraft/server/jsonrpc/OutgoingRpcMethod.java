package net.minecraft.server.jsonrpc;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import net.minecraft.server.jsonrpc.api.Schema;
import org.jspecify.annotations.Nullable;

public interface OutgoingRpcMethod<Params, Result> {
   String NOTIFICATION_PREFIX = "notification/";

   MethodInfo<Params, Result> info();

   OutgoingRpcMethod.Attributes attributes();

   @Nullable
   default JsonElement encodeParams(Params $$0) {
      return null;
   }

   @Nullable
   default Result decodeResult(JsonElement $$0) {
      return null;
   }

   static OutgoingRpcMethod.OutgoingRpcMethodBuilder<Void, Void> notification() {
      return new OutgoingRpcMethod.OutgoingRpcMethodBuilder<>(OutgoingRpcMethod.ParmeterlessNotification::new);
   }

   static <Params> OutgoingRpcMethod.OutgoingRpcMethodBuilder<Params, Void> notificationWithParams() {
      return new OutgoingRpcMethod.OutgoingRpcMethodBuilder<>(OutgoingRpcMethod.Notification::new);
   }

   static <Result> OutgoingRpcMethod.OutgoingRpcMethodBuilder<Void, Result> request() {
      return new OutgoingRpcMethod.OutgoingRpcMethodBuilder<>(OutgoingRpcMethod.ParameterlessMethod::new);
   }

   static <Params, Result> OutgoingRpcMethod.OutgoingRpcMethodBuilder<Params, Result> requestWithParams() {
      return new OutgoingRpcMethod.OutgoingRpcMethodBuilder<>(OutgoingRpcMethod.Method::new);
   }

   public record Attributes(boolean discoverable) {
   }

   @FunctionalInterface
   public interface Factory<Params, Result> {
      OutgoingRpcMethod<Params, Result> create(MethodInfo<Params, Result> var1, OutgoingRpcMethod.Attributes var2);
   }

   public record Method<Params, Result>(MethodInfo<Params, Result> info, OutgoingRpcMethod.Attributes attributes) implements OutgoingRpcMethod<Params, Result> {
      @Nullable
      @Override
      public JsonElement encodeParams(Params $$0) {
         if (this.info.params().isEmpty()) {
            throw new IllegalStateException("Method defined as having no parameters");
         } else {
            return (JsonElement)this.info.params().get().schema().codec().encodeStart(JsonOps.INSTANCE, $$0).getOrThrow();
         }
      }

      @Override
      public Result decodeResult(JsonElement $$0) {
         if (this.info.result().isEmpty()) {
            throw new IllegalStateException("Method defined as having no result");
         } else {
            return (Result)this.info.result().get().schema().codec().parse(JsonOps.INSTANCE, $$0).getOrThrow();
         }
      }
   }

   public record Notification<Params>(MethodInfo<Params, Void> info, OutgoingRpcMethod.Attributes attributes) implements OutgoingRpcMethod<Params, Void> {
      @Nullable
      @Override
      public JsonElement encodeParams(Params $$0) {
         if (this.info.params().isEmpty()) {
            throw new IllegalStateException("Method defined as having no parameters");
         } else {
            return (JsonElement)this.info.params().get().schema().codec().encodeStart(JsonOps.INSTANCE, $$0).getOrThrow();
         }
      }
   }

   public static class OutgoingRpcMethodBuilder<Params, Result> {
      public static final OutgoingRpcMethod.Attributes DEFAULT_ATTRIBUTES = new OutgoingRpcMethod.Attributes(true);
      private final OutgoingRpcMethod.Factory<Params, Result> method;
      private String description = "";
      @Nullable
      private ParamInfo<Params> paramInfo;
      @Nullable
      private ResultInfo<Result> resultInfo;

      public OutgoingRpcMethodBuilder(OutgoingRpcMethod.Factory<Params, Result> $$0) {
         this.method = $$0;
      }

      public OutgoingRpcMethod.OutgoingRpcMethodBuilder<Params, Result> description(String $$0) {
         this.description = $$0;
         return this;
      }

      public OutgoingRpcMethod.OutgoingRpcMethodBuilder<Params, Result> response(String $$0, Schema<Result> $$1) {
         this.resultInfo = new ResultInfo<>($$0, $$1);
         return this;
      }

      public OutgoingRpcMethod.OutgoingRpcMethodBuilder<Params, Result> param(String $$0, Schema<Params> $$1) {
         this.paramInfo = new ParamInfo<>($$0, $$1);
         return this;
      }

      private OutgoingRpcMethod<Params, Result> build() {
         MethodInfo<Params, Result> $$0 = new MethodInfo<>(this.description, this.paramInfo, this.resultInfo);
         return this.method.create($$0, DEFAULT_ATTRIBUTES);
      }

      public Reference<OutgoingRpcMethod<Params, Result>> register(String $$0) {
         return this.register(Identifier.withDefaultNamespace("notification/" + $$0));
      }

      private Reference<OutgoingRpcMethod<Params, Result>> register(Identifier $$0) {
         return Registry.registerForHolder(BuiltInRegistries.OUTGOING_RPC_METHOD, $$0, this.build());
      }
   }

   public record ParameterlessMethod<Result>(MethodInfo<Void, Result> info, OutgoingRpcMethod.Attributes attributes) implements OutgoingRpcMethod<Void, Result> {
      @Override
      public Result decodeResult(JsonElement $$0) {
         if (this.info.result().isEmpty()) {
            throw new IllegalStateException("Method defined as having no result");
         } else {
            return (Result)this.info.result().get().schema().codec().parse(JsonOps.INSTANCE, $$0).getOrThrow();
         }
      }
   }

   public record ParmeterlessNotification(MethodInfo<Void, Void> info, OutgoingRpcMethod.Attributes attributes) implements OutgoingRpcMethod<Void, Void> {
   }
}
