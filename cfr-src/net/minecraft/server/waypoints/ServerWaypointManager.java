/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.google.common.collect.Table
 *  com.google.common.collect.Tables
 */
package net.minecraft.server.waypoints;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointManager;
import net.minecraft.world.waypoints.WaypointTransmitter;

public class ServerWaypointManager
implements WaypointManager<WaypointTransmitter> {
    private final Set<WaypointTransmitter> waypoints = new HashSet<WaypointTransmitter>();
    private final Set<ServerPlayer> players = new HashSet<ServerPlayer>();
    private final Table<ServerPlayer, WaypointTransmitter, WaypointTransmitter.Connection> connections = HashBasedTable.create();

    @Override
    public void trackWaypoint(WaypointTransmitter $$0) {
        this.waypoints.add($$0);
        for (ServerPlayer $$1 : this.players) {
            this.createConnection($$1, $$0);
        }
    }

    @Override
    public void updateWaypoint(WaypointTransmitter $$0) {
        if (!this.waypoints.contains($$0)) {
            return;
        }
        Map $$1 = Tables.transpose(this.connections).row((Object)$$0);
        Sets.SetView $$2 = Sets.difference(this.players, $$1.keySet());
        for (Map.Entry $$3 : ImmutableSet.copyOf($$1.entrySet())) {
            this.updateConnection((ServerPlayer)$$3.getKey(), $$0, (WaypointTransmitter.Connection)$$3.getValue());
        }
        for (ServerPlayer $$4 : $$2) {
            this.createConnection($$4, $$0);
        }
    }

    @Override
    public void untrackWaypoint(WaypointTransmitter $$02) {
        this.connections.column((Object)$$02).forEach(($$0, $$1) -> $$1.disconnect());
        Tables.transpose(this.connections).row((Object)$$02).clear();
        this.waypoints.remove($$02);
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
        Map $$1 = this.connections.row((Object)$$0);
        Sets.SetView $$2 = Sets.difference(this.waypoints, $$1.keySet());
        for (Map.Entry $$3 : ImmutableSet.copyOf($$1.entrySet())) {
            this.updateConnection($$0, (WaypointTransmitter)$$3.getKey(), (WaypointTransmitter.Connection)$$3.getValue());
        }
        for (WaypointTransmitter $$4 : $$2) {
            this.createConnection($$0, $$4);
        }
    }

    public void removePlayer(ServerPlayer $$02) {
        this.connections.row((Object)$$02).values().removeIf($$0 -> {
            $$0.disconnect();
            return true;
        });
        this.untrackWaypoint($$02);
        this.players.remove($$02);
    }

    public void breakAllConnections() {
        this.connections.values().forEach(WaypointTransmitter.Connection::disconnect);
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
        return $$0.level().getGameRules().get(GameRules.LOCATOR_BAR);
    }

    private void createConnection(ServerPlayer $$0, WaypointTransmitter $$1) {
        if ($$0 == $$1) {
            return;
        }
        if (!ServerWaypointManager.isLocatorBarEnabledFor($$0)) {
            return;
        }
        $$1.makeWaypointConnectionWith($$0).ifPresentOrElse($$2 -> {
            this.connections.put((Object)$$0, (Object)$$1, $$2);
            $$2.connect();
        }, () -> {
            WaypointTransmitter.Connection $$2 = (WaypointTransmitter.Connection)this.connections.remove((Object)$$0, (Object)$$1);
            if ($$2 != null) {
                $$2.disconnect();
            }
        });
    }

    private void updateConnection(ServerPlayer $$0, WaypointTransmitter $$1, WaypointTransmitter.Connection $$22) {
        if ($$0 == $$1) {
            return;
        }
        if (!ServerWaypointManager.isLocatorBarEnabledFor($$0)) {
            return;
        }
        if (!$$22.isBroken()) {
            $$22.update();
            return;
        }
        $$1.makeWaypointConnectionWith($$0).ifPresentOrElse($$2 -> {
            $$2.connect();
            this.connections.put((Object)$$0, (Object)$$1, $$2);
        }, () -> {
            $$22.disconnect();
            this.connections.remove((Object)$$0, (Object)$$1);
        });
    }

    @Override
    public /* synthetic */ void untrackWaypoint(Waypoint waypoint) {
        this.untrackWaypoint((WaypointTransmitter)waypoint);
    }

    @Override
    public /* synthetic */ void trackWaypoint(Waypoint waypoint) {
        this.trackWaypoint((WaypointTransmitter)waypoint);
    }
}

