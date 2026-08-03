package net.minecraft.server.jsonrpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

public class JsonRPCUtils {
   public static final String JSON_RPC_VERSION = "2.0";
   public static final String OPEN_RPC_VERSION = "1.3.2";

   public static JsonObject createSuccessResult(JsonElement $$0, JsonElement $$1) {
      JsonObject $$2 = new JsonObject();
      $$2.addProperty("jsonrpc", "2.0");
      $$2.add("id", $$0);
      $$2.add("result", $$1);
      return $$2;
   }

   public static JsonObject createRequest(@Nullable Integer $$0, Identifier $$1, List<JsonElement> $$2) {
      JsonObject $$3 = new JsonObject();
      $$3.addProperty("jsonrpc", "2.0");
      if ($$0 != null) {
         $$3.addProperty("id", $$0);
      }

      $$3.addProperty("method", $$1.toString());
      if (!$$2.isEmpty()) {
         JsonArray $$4 = new JsonArray($$2.size());

         for (JsonElement $$5 : $$2) {
            $$4.add($$5);
         }

         $$3.add("params", $$4);
      }

      return $$3;
   }

   public static JsonObject createError(JsonElement $$0, String $$1, int $$2, @Nullable String $$3) {
      JsonObject $$4 = new JsonObject();
      $$4.addProperty("jsonrpc", "2.0");
      $$4.add("id", $$0);
      JsonObject $$5 = new JsonObject();
      $$5.addProperty("code", $$2);
      $$5.addProperty("message", $$1);
      if ($$3 != null && !$$3.isBlank()) {
         $$5.addProperty("data", $$3);
      }

      $$4.add("error", $$5);
      return $$4;
   }

   @Nullable
   public static JsonElement getRequestId(JsonObject $$0) {
      return $$0.get("id");
   }

   @Nullable
   public static String getMethodName(JsonObject $$0) {
      return GsonHelper.getAsString($$0, "method", null);
   }

   @Nullable
   public static JsonElement getParams(JsonObject $$0) {
      return $$0.get("params");
   }

   @Nullable
   public static JsonElement getResult(JsonObject $$0) {
      return $$0.get("result");
   }

   @Nullable
   public static JsonObject getError(JsonObject $$0) {
      return GsonHelper.getAsJsonObject($$0, "error", null);
   }
}
