package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Objects;
import net.minecraft.server.notifications.NotificationService;

public class UserBanList extends StoredUserList<NameAndId, UserBanListEntry> {
   public UserBanList(File $$0, NotificationService $$1) {
      super($$0, $$1);
   }

   @Override
   protected StoredUserEntry<NameAndId> createEntry(JsonObject $$0) {
      return new UserBanListEntry($$0);
   }

   public boolean isBanned(NameAndId $$0) {
      return this.contains($$0);
   }

   @Override
   public String[] getUserList() {
      return this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(NameAndId::name).toArray(String[]::new);
   }

   protected String getKeyForUser(NameAndId $$0) {
      return $$0.id().toString();
   }

   public boolean add(UserBanListEntry $$0) {
      if (super.add($$0)) {
         if ($$0.getUser() != null) {
            this.notificationService.playerBanned($$0);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean remove(NameAndId $$0) {
      if (super.remove($$0)) {
         this.notificationService.playerUnbanned($$0);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void clear() {
      for (UserBanListEntry $$0 : this.getEntries()) {
         if ($$0.getUser() != null) {
            this.notificationService.playerUnbanned($$0.getUser());
         }
      }

      super.clear();
   }
}
