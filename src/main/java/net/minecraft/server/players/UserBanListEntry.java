package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.util.Date;
import net.minecraft.network.chat.Component;

public class UserBanListEntry extends BanListEntry<NameAndId> {
   private static final Component MESSAGE_UNKNOWN_USER = Component.translatable("commands.banlist.entry.unknown");

   public UserBanListEntry(NameAndId $$0) {
      this($$0, null, null, null, null);
   }

   public UserBanListEntry(NameAndId $$0, Date $$1, String $$2, Date $$3, String $$4) {
      super($$0, $$1, $$2, $$3, $$4);
   }

   public UserBanListEntry(JsonObject $$0) {
      super(NameAndId.fromJson($$0), $$0);
   }

   @Override
   protected void serialize(JsonObject $$0) {
      if (this.getUser() != null) {
         this.getUser().appendTo($$0);
         super.serialize($$0);
      }
   }

   @Override
   public Component getDisplayName() {
      NameAndId $$0 = this.getUser();
      return (Component)($$0 != null ? Component.literal($$0.name()) : MESSAGE_UNKNOWN_USER);
   }
}
