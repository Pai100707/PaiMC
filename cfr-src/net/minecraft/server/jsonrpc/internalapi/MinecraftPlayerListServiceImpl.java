/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc.internalapi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.internalapi.MinecraftPlayerListService;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import org.jspecify.annotations.Nullable;

public class MinecraftPlayerListServiceImpl
implements MinecraftPlayerListService {
    private final JsonRpcLogger jsonRpcLogger;
    private final DedicatedServer server;

    public MinecraftPlayerListServiceImpl(DedicatedServer $$0, JsonRpcLogger $$1) {
        this.jsonRpcLogger = $$1;
        this.server = $$0;
    }

    @Override
    public List<ServerPlayer> getPlayers() {
        return this.server.getPlayerList().getPlayers();
    }

    @Override
    public @Nullable ServerPlayer getPlayer(UUID $$0) {
        return this.server.getPlayerList().getPlayer($$0);
    }

    @Override
    public Optional<NameAndId> fetchUserByName(String $$0) {
        return this.server.services().nameToIdCache().get($$0);
    }

    @Override
    public Optional<NameAndId> fetchUserById(UUID $$02) {
        return Optional.ofNullable(this.server.services().sessionService().fetchProfile($$02, true)).map($$0 -> new NameAndId($$0.profile()));
    }

    @Override
    public Optional<NameAndId> getCachedUserById(UUID $$0) {
        return this.server.services().nameToIdCache().get($$0);
    }

    @Override
    public Optional<ServerPlayer> getPlayer(Optional<UUID> $$0, Optional<String> $$1) {
        if ($$0.isPresent()) {
            return Optional.ofNullable(this.server.getPlayerList().getPlayer($$0.get()));
        }
        if ($$1.isPresent()) {
            return Optional.ofNullable(this.server.getPlayerList().getPlayerByName($$1.get()));
        }
        return Optional.empty();
    }

    @Override
    public List<ServerPlayer> getPlayersWithAddress(String $$0) {
        return this.server.getPlayerList().getPlayersWithAddress($$0);
    }

    @Override
    public void remove(ServerPlayer $$0, ClientInfo $$1) {
        this.server.getPlayerList().remove($$0);
        this.jsonRpcLogger.log($$1, "Remove player '{}'", $$0.getPlainTextName());
    }

    @Override
    public @Nullable ServerPlayer getPlayerByName(String $$0) {
        return this.server.getPlayerList().getPlayerByName($$0);
    }
}

