/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 */
package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerMap {
    private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap();

    public Set<ServerPlayer> getAllPlayers() {
        return this.players.keySet();
    }

    public void addPlayer(ServerPlayer $$0, boolean $$1) {
        this.players.put((Object)$$0, $$1);
    }

    public void removePlayer(ServerPlayer $$0) {
        this.players.removeBoolean((Object)$$0);
    }

    public void ignorePlayer(ServerPlayer $$0) {
        this.players.replace((Object)$$0, true);
    }

    public void unIgnorePlayer(ServerPlayer $$0) {
        this.players.replace((Object)$$0, false);
    }

    public boolean ignoredOrUnknown(ServerPlayer $$0) {
        return this.players.getOrDefault((Object)$$0, true);
    }

    public boolean ignored(ServerPlayer $$0) {
        return this.players.getBoolean((Object)$$0);
    }
}

