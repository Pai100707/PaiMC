package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.util.Date;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class UserBanListEntry extends BanListEntry<NameAndId> {
   private static final Component MESSAGE_UNKNOWN_USER = Component.translatable("commands.banlist.entry.unknown");

   public UserBanListEntry(@Nullable NameAndId $$0) {
      this($$0, null, null, null, null);
   }

   public UserBanListEntry(@Nullable NameAndId $$0, @Nullable Date $$1, @Nullable String $$2, @Nullable Date $$3, @Nullable String $$4) {
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
