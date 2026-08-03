package net.minecraft.server.players;

import com.google.gson.JsonObject;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;

public class ServerOpListEntry extends StoredUserEntry<NameAndId> {
   private final LevelBasedPermissionSet permissions;
   private final boolean bypassesPlayerLimit;

   public ServerOpListEntry(NameAndId $$0, LevelBasedPermissionSet $$1, boolean $$2) {
      super($$0);
      this.permissions = $$1;
      this.bypassesPlayerLimit = $$2;
   }

   public ServerOpListEntry(JsonObject $$0) {
      super(NameAndId.fromJson($$0));
      PermissionLevel $$1 = $$0.has("level") ? PermissionLevel.byId($$0.get("level").getAsInt()) : PermissionLevel.ALL;
      this.permissions = LevelBasedPermissionSet.forLevel($$1);
      this.bypassesPlayerLimit = $$0.has("bypassesPlayerLimit") && $$0.get("bypassesPlayerLimit").getAsBoolean();
   }

   public LevelBasedPermissionSet permissions() {
      return this.permissions;
   }

   public boolean getBypassesPlayerLimit() {
      return this.bypassesPlayerLimit;
   }

   @Override
   protected void serialize(JsonObject $$0) {
      if (this.getUser() != null) {
         this.getUser().appendTo($$0);
         $$0.addProperty("level", this.permissions.level().id());
         $$0.addProperty("bypassesPlayerLimit", this.bypassesPlayerLimit);
      }
   }
}
