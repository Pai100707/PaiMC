/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.internalapi;

import java.util.Collection;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.internalapi.MinecraftServerStateService;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class MinecraftServerStateServiceImpl
implements MinecraftServerStateService {
    private final DedicatedServer server;
    private final JsonRpcLogger jsonrpcLogger;

    public MinecraftServerStateServiceImpl(DedicatedServer $$0, JsonRpcLogger $$1) {
        this.server = $$0;
        this.jsonrpcLogger = $$1;
    }

    @Override
    public boolean isReady() {
        return this.server.isReady();
    }

    @Override
    public boolean saveEverything(boolean $$0, boolean $$1, boolean $$2, ClientInfo $$3) {
        this.jsonrpcLogger.log($$3, "Save everything. SuppressLogs: {}, flush: {}, force: {}", $$0, $$1, $$2);
        return this.server.saveEverything($$0, $$1, $$2);
    }

    @Override
    public void halt(boolean $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Halt server. WaitForShutdown: {}", $$0);
        this.server.halt($$0);
    }

    @Override
    public void sendSystemMessage(Component $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Send system message: '{}'", $$0.getString());
        this.server.sendSystemMessage($$0);
    }

    @Override
    public void sendSystemMessage(Component $$0, boolean $$1, Collection<ServerPlayer> $$2, ClientInfo $$3) {
        List<String> $$4 = $$2.stream().map(Player::getPlainTextName).toList();
        this.jsonrpcLogger.log($$3, "Send system message to '{}' players (overlay: {}): '{}'", $$4.size(), $$1, $$0.getString());
        for (ServerPlayer $$5 : $$2) {
            if ($$1) {
                $$5.sendSystemMessage($$0, true);
                continue;
            }
            $$5.sendSystemMessage($$0);
        }
    }

    @Override
    public void broadcastSystemMessage(Component $$0, boolean $$1, ClientInfo $$2) {
        this.jsonrpcLogger.log($$2, "Broadcast system message (overlay: {}): '{}'", $$1, $$0.getString());
        for (ServerPlayer $$3 : this.server.getPlayerList().getPlayers()) {
            if ($$1) {
                $$3.sendSystemMessage($$0, true);
                continue;
            }
            $$3.sendSystemMessage($$0);
        }
    }
}

