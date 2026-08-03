package net.minecraft.server.jsonrpc.internalapi;

import java.util.Collection;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserWhiteListEntry;

public class MinecraftAllowListServiceImpl implements MinecraftAllowListService {
   private final DedicatedServer server;
   private final JsonRpcLogger jsonrpcLogger;

   public MinecraftAllowListServiceImpl(DedicatedServer $$0, JsonRpcLogger $$1) {
      this.server = $$0;
      this.jsonrpcLogger = $$1;
   }

   @Override
   public Collection<UserWhiteListEntry> getEntries() {
      return this.server.getPlayerList().getWhiteList().getEntries();
   }

   @Override
   public boolean add(UserWhiteListEntry $$0, ClientInfo $$1) {
      this.jsonrpcLogger.log($$1, "Add player '{}' to allowlist", $$0.getUser());
      return this.server.getPlayerList().getWhiteList().add($$0);
   }

   @Override
   public void clear(ClientInfo $$0) {
      this.jsonrpcLogger.log($$0, "Clear allowlist");
      this.server.getPlayerList().getWhiteList().clear();
   }

   @Override
   public void remove(NameAndId $$0, ClientInfo $$1) {
      this.jsonrpcLogger.log($$1, "Remove player '{}' from allowlist", $$0);
      this.server.getPlayerList().getWhiteList().remove($$0);
   }

   @Override
   public void kickUnlistedPlayers(ClientInfo $$0) {
      this.jsonrpcLogger.log($$0, "Kick unlisted players");
      this.server.kickUnlistedPlayers();
   }
}
