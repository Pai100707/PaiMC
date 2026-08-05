package net.minecraft.server.jsonrpc.internalapi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Util;

public interface MinecraftPlayerListService {
   List<ServerPlayer> getPlayers();

   
   ServerPlayer getPlayer(UUID var1);

   default CompletableFuture<Optional<NameAndId>> getUser(Optional<UUID> $$0, Optional<String> $$1) {
      if ($$0.isPresent()) {
         Optional<NameAndId> $$2 = this.getCachedUserById($$0.get());
         return $$2.isPresent()
            ? CompletableFuture.completedFuture($$2)
            : CompletableFuture.supplyAsync(() -> this.fetchUserById($$0.get()), Util.nonCriticalIoPool());
      } else {
         return $$1.isPresent()
            ? CompletableFuture.supplyAsync(() -> this.fetchUserByName($$1.get()), Util.nonCriticalIoPool())
            : CompletableFuture.completedFuture(Optional.empty());
      }
   }

   Optional<NameAndId> fetchUserByName(String var1);

   Optional<NameAndId> fetchUserById(UUID var1);

   Optional<NameAndId> getCachedUserById(UUID var1);

   Optional<ServerPlayer> getPlayer(Optional<UUID> var1, Optional<String> var2);

   List<ServerPlayer> getPlayersWithAddress(String var1);

   
   ServerPlayer getPlayerByName(String var1);

   void remove(ServerPlayer var1, ClientInfo var2);
}
