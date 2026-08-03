package net.minecraft.server.jsonrpc.methods;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RemoteRpcErrorException extends RuntimeException {
   private final JsonElement id;
   private final JsonObject error;

   public RemoteRpcErrorException(JsonElement $$0, JsonObject $$1) {
      this.id = $$0;
      this.error = $$1;
   }

   private JsonObject getError() {
      return this.error;
   }

   private JsonElement getId() {
      return this.id;
   }
}
