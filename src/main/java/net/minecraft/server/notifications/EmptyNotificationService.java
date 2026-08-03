package net.minecraft.server.notifications;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.level.gamerules.GameRule;

public class EmptyNotificationService implements NotificationService {
   @Override
   public void playerJoined(ServerPlayer $$0) {
   }

   @Override
   public void playerLeft(ServerPlayer $$0) {
   }

   @Override
   public void serverStarted() {
   }

   @Override
   public void serverShuttingDown() {
   }

   @Override
   public void serverSaveStarted() {
   }

   @Override
   public void serverSaveCompleted() {
   }

   @Override
   public void serverActivityOccured() {
   }

   @Override
   public void playerOped(ServerOpListEntry $$0) {
   }

   @Override
   public void playerDeoped(ServerOpListEntry $$0) {
   }

   @Override
   public void playerAddedToAllowlist(NameAndId $$0) {
   }

   @Override
   public void playerRemovedFromAllowlist(NameAndId $$0) {
   }

   @Override
   public void ipBanned(IpBanListEntry $$0) {
   }

   @Override
   public void ipUnbanned(String $$0) {
   }

   @Override
   public void playerBanned(UserBanListEntry $$0) {
   }

   @Override
   public void playerUnbanned(NameAndId $$0) {
   }

   @Override
   public <T> void onGameRuleChanged(GameRule<T> $$0, T $$1) {
   }

   @Override
   public void statusHeartbeat() {
   }
}
