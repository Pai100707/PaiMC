package net.minecraft.server.waypoints;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.Sets.SetView;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.waypoints.WaypointManager;
import net.minecraft.world.waypoints.WaypointTransmitter;
import net.minecraft.world.waypoints.WaypointTransmitter.Connection;

public class ServerWaypointManager implements WaypointManager<WaypointTransmitter> {
   private final Set<WaypointTransmitter> waypoints = new HashSet<>();
   private final Set<ServerPlayer> players = new HashSet<>();
   private final Table<ServerPlayer, WaypointTransmitter, Connection> connections = HashBasedTable.create();

   public void trackWaypoint(WaypointTransmitter $$0) {
      this.waypoints.add($$0);

      for (ServerPlayer $$1 : this.players) {
         this.createConnection($$1, $$0);
      }
   }

   public void updateWaypoint(WaypointTransmitter $$0) {
      if (this.waypoints.contains($$0)) {
         Map<ServerPlayer, Connection> $$1 = Tables.transpose(this.connections).row($$0);
         SetView<ServerPlayer> $$2 = Sets.difference(this.players, $$1.keySet());
         UnmodifiableIterator var4 = ImmutableSet.copyOf($$1.entrySet()).iterator();

         while (var4.hasNext()) {
            Entry<ServerPlayer, Connection> $$3 = (Entry<ServerPlayer, Connection>)var4.next();
            this.updateConnection($$3.getKey(), $$0, $$3.getValue());
         }

         var4 = $$2.iterator();

         while (var4.hasNext()) {
            ServerPlayer $$4 = (ServerPlayer)var4.next();
            this.createConnection($$4, $$0);
         }
      }
   }

   public void untrackWaypoint(WaypointTransmitter $$0) {
      this.connections.column($$0).forEach(($$0x, $$1) -> $$1.disconnect());
      Tables.transpose(this.connections).row($$0).clear();
      this.waypoints.remove($$0);
   }

   public void addPlayer(ServerPlayer $$0) {
      this.players.add($$0);

      for (WaypointTransmitter $$1 : this.waypoints) {
         this.createConnection($$0, $$1);
      }

      if ($$0.isTransmittingWaypoint()) {
         this.trackWaypoint($$0);
      }
   }

   public void updatePlayer(ServerPlayer $$0) {
      Map<WaypointTransmitter, Connection> $$1 = this.connections.row($$0);
      SetView<WaypointTransmitter> $$2 = Sets.difference(this.waypoints, $$1.keySet());
      UnmodifiableIterator var4 = ImmutableSet.copyOf($$1.entrySet()).iterator();

      while (var4.hasNext()) {
         Entry<WaypointTransmitter, Connection> $$3 = (Entry<WaypointTransmitter, Connection>)var4.next();
         this.updateConnection($$0, $$3.getKey(), $$3.getValue());
      }

      var4 = $$2.iterator();

      while (var4.hasNext()) {
         WaypointTransmitter $$4 = (WaypointTransmitter)var4.next();
         this.createConnection($$0, $$4);
      }
   }

   public void removePlayer(ServerPlayer $$0) {
      this.connections.row($$0).values().removeIf($$0x -> {
         $$0x.disconnect();
         return true;
      });
      this.untrackWaypoint($$0);
      this.players.remove($$0);
   }

   public void breakAllConnections() {
      this.connections.values().forEach(Connection::disconnect);
      this.connections.clear();
   }

   public void remakeConnections(WaypointTransmitter $$0) {
      for (ServerPlayer $$1 : this.players) {
         this.createConnection($$1, $$0);
      }
   }

   public Set<WaypointTransmitter> transmitters() {
      return this.waypoints;
   }

   private static boolean isLocatorBarEnabledFor(ServerPlayer $$0) {
      return (Boolean)$$0.level().getGameRules().get(GameRules.LOCATOR_BAR);
   }

   private void createConnection(ServerPlayer $$0, WaypointTransmitter $$1) {
      if ($$0 != $$1) {
         if (isLocatorBarEnabledFor($$0)) {
            $$1.makeWaypointConnectionWith($$0).ifPresentOrElse($$2 -> {
               this.connections.put($$0, $$1, $$2);
               $$2.connect();
            }, () -> {
               Connection $$2 = (Connection)this.connections.remove($$0, $$1);
               if ($$2 != null) {
                  $$2.disconnect();
               }
            });
         }
      }
   }

   private void updateConnection(ServerPlayer $$0, WaypointTransmitter $$1, Connection $$2) {
      if ($$0 != $$1) {
         if (isLocatorBarEnabledFor($$0)) {
            if (!$$2.isBroken()) {
               $$2.update();
            } else {
               $$1.makeWaypointConnectionWith($$0).ifPresentOrElse($$2x -> {
                  $$2x.connect();
                  this.connections.put($$0, $$1, $$2x);
               }, () -> {
                  $$2.disconnect();
                  this.connections.remove($$0, $$1);
               });
            }
         }
      }
   }
}
