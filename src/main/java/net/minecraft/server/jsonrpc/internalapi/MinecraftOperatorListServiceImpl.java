package net.minecraft.server.jsonrpc.internalapi;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.ServerOpListEntry;

public class MinecraftOperatorListServiceImpl implements MinecraftOperatorListService {
   private final net.minecraft.server.MinecraftServer minecraftServer;
   private final JsonRpcLogger jsonrpcLogger;

   public MinecraftOperatorListServiceImpl(net.minecraft.server.MinecraftServer $$0, JsonRpcLogger $$1) {
      this.minecraftServer = $$0;
      this.jsonrpcLogger = $$1;
   }

   @Override
   public Collection<ServerOpListEntry> getEntries() {
      return this.minecraftServer.getPlayerList().getOps().getEntries();
   }

   @Override
   public void op(NameAndId $$0, Optional<PermissionLevel> $$1, Optional<Boolean> $$2, ClientInfo $$3) {
      this.jsonrpcLogger.log($$3, "Op '{}'", $$0);
      this.minecraftServer.getPlayerList().op($$0, $$1.map(LevelBasedPermissionSet::forLevel), $$2);
   }

   @Override
   public void op(NameAndId $$0, ClientInfo $$1) {
      this.jsonrpcLogger.log($$1, "Op '{}'", $$0);
      this.minecraftServer.getPlayerList().op($$0);
   }

   @Override
   public void deop(NameAndId $$0, ClientInfo $$1) {
      this.jsonrpcLogger.log($$1, "Deop '{}'", $$0);
      this.minecraftServer.getPlayerList().deop($$0);
   }

   @Override
   public void clear(ClientInfo $$0) {
      this.jsonrpcLogger.log($$0, "Clear operator list");
      this.minecraftServer.getPlayerList().getOps().clear();
   }
}
