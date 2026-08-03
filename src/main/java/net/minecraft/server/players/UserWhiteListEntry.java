package net.minecraft.server.players;

import com.google.gson.JsonObject;

public class UserWhiteListEntry extends StoredUserEntry<NameAndId> {
   public UserWhiteListEntry(NameAndId $$0) {
      super($$0);
   }

   public UserWhiteListEntry(JsonObject $$0) {
      super(NameAndId.fromJson($$0));
   }

   @Override
   protected void serialize(JsonObject $$0) {
      if (this.getUser() != null) {
         this.getUser().appendTo($$0);
      }
   }
}
