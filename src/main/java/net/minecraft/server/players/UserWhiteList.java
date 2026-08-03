package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Objects;
import net.minecraft.server.notifications.NotificationService;

public class UserWhiteList extends StoredUserList<NameAndId, UserWhiteListEntry> {
   public UserWhiteList(File $$0, NotificationService $$1) {
      super($$0, $$1);
   }

   @Override
   protected StoredUserEntry<NameAndId> createEntry(JsonObject $$0) {
      return new UserWhiteListEntry($$0);
   }

   public boolean isWhiteListed(NameAndId $$0) {
      return this.contains($$0);
   }

   public boolean add(UserWhiteListEntry $$0) {
      if (super.add($$0)) {
         if ($$0.getUser() != null) {
            this.notificationService.playerAddedToAllowlist($$0.getUser());
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean remove(NameAndId $$0) {
      if (super.remove($$0)) {
         this.notificationService.playerRemovedFromAllowlist($$0);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void clear() {
      for (UserWhiteListEntry $$0 : this.getEntries()) {
         if ($$0.getUser() != null) {
            this.notificationService.playerRemovedFromAllowlist($$0.getUser());
         }
      }

      super.clear();
   }

   @Override
   public String[] getUserList() {
      return this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(NameAndId::name).toArray(String[]::new);
   }

   protected String getKeyForUser(NameAndId $$0) {
      return $$0.id().toString();
   }
}
