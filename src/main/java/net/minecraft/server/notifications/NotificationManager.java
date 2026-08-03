package net.minecraft.server.notifications;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.level.gamerules.GameRule;

public class NotificationManager implements NotificationService {
   private final List<NotificationService> notificationServices = Lists.newArrayList();

   public void registerService(NotificationService $$0) {
      this.notificationServices.add($$0);
   }

   @Override
   public void playerJoined(ServerPlayer $$0) {
      this.notificationServices.forEach($$1 -> $$1.playerJoined($$0));
   }

   @Override
   public void playerLeft(ServerPlayer $$0) {
      this.notificationServices.forEach($$1 -> $$1.playerLeft($$0));
   }

   @Override
   public void serverStarted() {
      this.notificationServices.forEach(NotificationService::serverStarted);
   }

   @Override
   public void serverShuttingDown() {
      this.notificationServices.forEach(NotificationService::serverShuttingDown);
   }

   @Override
   public void serverSaveStarted() {
      this.notificationServices.forEach(NotificationService::serverSaveStarted);
   }

   @Override
   public void serverSaveCompleted() {
      this.notificationServices.forEach(NotificationService::serverSaveCompleted);
   }

   @Override
   public void serverActivityOccured() {
      this.notificationServices.forEach(NotificationService::serverActivityOccured);
   }

   @Override
   public void playerOped(ServerOpListEntry $$0) {
      this.notificationServices.forEach($$1 -> $$1.playerOped($$0));
   }

   @Override
   public void playerDeoped(ServerOpListEntry $$0) {
      this.notificationServices.forEach($$1 -> $$1.playerDeoped($$0));
   }

   @Override
   public void playerAddedToAllowlist(NameAndId $$0) {
      this.notificationServices.forEach($$1 -> $$1.playerAddedToAllowlist($$0));
   }

   @Override
   public void playerRemovedFromAllowlist(NameAndId $$0) {
      this.notificationServices.forEach($$1 -> $$1.playerRemovedFromAllowlist($$0));
   }

   @Override
   public void ipBanned(IpBanListEntry $$0) {
      this.notificationServices.forEach($$1 -> $$1.ipBanned($$0));
   }

   @Override
   public void ipUnbanned(String $$0) {
      this.notificationServices.forEach($$1 -> $$1.ipUnbanned($$0));
   }

   @Override
   public void playerBanned(UserBanListEntry $$0) {
      this.notificationServices.forEach($$1 -> $$1.playerBanned($$0));
   }

   @Override
   public void playerUnbanned(NameAndId $$0) {
      this.notificationServices.forEach($$1 -> $$1.playerUnbanned($$0));
   }

   @Override
   public <T> void onGameRuleChanged(GameRule<T> $$0, T $$1) {
      this.notificationServices.forEach($$2 -> $$2.onGameRuleChanged($$0, $$1));
   }

   @Override
   public void statusHeartbeat() {
      this.notificationServices.forEach(NotificationService::statusHeartbeat);
   }
}
