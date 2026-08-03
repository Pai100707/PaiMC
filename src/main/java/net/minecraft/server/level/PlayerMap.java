package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;

public final class PlayerMap {
   private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap();

   public Set<ServerPlayer> getAllPlayers() {
      return this.players.keySet();
   }

   public void addPlayer(ServerPlayer $$0, boolean $$1) {
      this.players.put($$0, $$1);
   }

   public void removePlayer(ServerPlayer $$0) {
      this.players.removeBoolean($$0);
   }

   public void ignorePlayer(ServerPlayer $$0) {
      this.players.replace($$0, true);
   }

   public void unIgnorePlayer(ServerPlayer $$0) {
      this.players.replace($$0, false);
   }

   public boolean ignoredOrUnknown(ServerPlayer $$0) {
      return this.players.getOrDefault($$0, true);
   }

   public boolean ignored(ServerPlayer $$0) {
      return this.players.getBoolean($$0);
   }
}
