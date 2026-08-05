package net.minecraft.server.players;

import com.google.gson.JsonObject;

public abstract class StoredUserEntry<T> {
   
   private final T user;

   public StoredUserEntry(T $$0) {
      this.user = $$0;
   }

   
   public T getUser() {
      return this.user;
   }

   boolean hasExpired() {
      return false;
   }

   protected abstract void serialize(JsonObject var1);
}
