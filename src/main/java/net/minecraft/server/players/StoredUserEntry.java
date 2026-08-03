package net.minecraft.server.players;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public abstract class StoredUserEntry<T> {
   @Nullable
   private final T user;

   public StoredUserEntry(@Nullable T $$0) {
      this.user = $$0;
   }

   @Nullable
   public T getUser() {
      return this.user;
   }

   boolean hasExpired() {
      return false;
   }

   protected abstract void serialize(JsonObject var1);
}
