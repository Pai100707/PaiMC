/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.internalapi;

import java.util.Collection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.internalapi.MinecraftBanListService;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanListEntry;

public class MinecraftBanListServiceImpl
implements MinecraftBanListService {
    private final MinecraftServer server;
    private final JsonRpcLogger jsonrpcLogger;

    public MinecraftBanListServiceImpl(MinecraftServer $$0, JsonRpcLogger $$1) {
        this.server = $$0;
        this.jsonrpcLogger = $$1;
    }

    @Override
    public void addUserBan(UserBanListEntry $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Add player '{}' to banlist. Reason: '{}'", $$0.getDisplayName(), $$0.getReasonMessage().getString());
        this.server.getPlayerList().getBans().add($$0);
    }

    @Override
    public void removeUserBan(NameAndId $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Remove player '{}' from banlist", $$0);
        this.server.getPlayerList().getBans().remove($$0);
    }

    @Override
    public void clearUserBans(ClientInfo $$0) {
        this.server.getPlayerList().getBans().clear();
    }

    @Override
    public Collection<UserBanListEntry> getUserBanEntries() {
        return this.server.getPlayerList().getBans().getEntries();
    }

    @Override
    public Collection<IpBanListEntry> getIpBanEntries() {
        return this.server.getPlayerList().getIpBans().getEntries();
    }

    @Override
    public void addIpBan(IpBanListEntry $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Add ip '{}' to ban list", $$0.getUser());
        this.server.getPlayerList().getIpBans().add($$0);
    }

    @Override
    public void clearIpBans(ClientInfo $$0) {
        this.jsonrpcLogger.log($$0, "Clear ip ban list", new Object[0]);
        this.server.getPlayerList().getIpBans().clear();
    }

    @Override
    public void removeIpBan(String $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Remove ip '{}' from ban list", $$0);
        this.server.getPlayerList().getIpBans().remove($$0);
    }
}

