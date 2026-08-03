package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.util.Date;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class IpBanListEntry extends BanListEntry<String> {
   public IpBanListEntry(String $$0) {
      this($$0, null, null, null, null);
   }

   public IpBanListEntry(String $$0, @Nullable Date $$1, @Nullable String $$2, @Nullable Date $$3, @Nullable String $$4) {
      super($$0, $$1, $$2, $$3, $$4);
   }

   @Override
   public Component getDisplayName() {
      return Component.literal(String.valueOf(this.getUser()));
   }

   public IpBanListEntry(JsonObject $$0) {
      super(createIpInfo($$0), $$0);
   }

   private static String createIpInfo(JsonObject $$0) {
      return $$0.has("ip") ? $$0.get("ip").getAsString() : null;
   }

   @Override
   protected void serialize(JsonObject $$0) {
      if (this.getUser() != null) {
         $$0.addProperty("ip", this.getUser());
         super.serialize($$0);
      }
   }
}
