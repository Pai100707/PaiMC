package net.minecraft.server.jsonrpc;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public enum JsonRPCErrors {
   PARSE_ERROR(-32700, "Parse error"),
   INVALID_REQUEST(-32600, "Invalid Request"),
   METHOD_NOT_FOUND(-32601, "Method not found"),
   INVALID_PARAMS(-32602, "Invalid params"),
   INTERNAL_ERROR(-32603, "Internal error");

   private final int errorCode;
   private final String message;

   private JsonRPCErrors(final int $$0, final String $$1) {
      this.errorCode = $$0;
      this.message = $$1;
   }

   public JsonObject createWithUnknownId(@Nullable String $$0) {
      return JsonRPCUtils.createError(JsonNull.INSTANCE, this.message, this.errorCode, $$0);
   }

   public JsonObject createWithoutData(JsonElement $$0) {
      return JsonRPCUtils.createError($$0, this.message, this.errorCode, null);
   }

   public JsonObject create(JsonElement $$0, String $$1) {
      return JsonRPCUtils.createError($$0, this.message, this.errorCode, $$1);
   }
}
