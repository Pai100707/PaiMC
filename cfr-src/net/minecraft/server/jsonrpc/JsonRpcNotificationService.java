/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc;

import net.minecraft.core.Holder;
import net.minecraft.server.jsonrpc.ManagementServer;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;
import net.minecraft.server.jsonrpc.OutgoingRpcMethods;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.BanlistService;
import net.minecraft.server.jsonrpc.methods.GameRulesService;
import net.minecraft.server.jsonrpc.methods.IpBanlistService;
import net.minecraft.server.jsonrpc.methods.OperatorService;
import net.minecraft.server.jsonrpc.methods.ServerStateService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.notifications.NotificationService;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.level.gamerules.GameRule;

public class JsonRpcNotificationService
implements NotificationService {
    private final ManagementServer managementServer;
    private final MinecraftApi minecraftApi;

    public JsonRpcNotificationService(MinecraftApi $$0, ManagementServer $$1) {
        this.minecraftApi = $$0;
        this.managementServer = $$1;
    }

    @Override
    public void playerJoined(ServerPlayer $$0) {
        this.broadcastNotification(OutgoingRpcMethods.PLAYER_JOINED, PlayerDto.from($$0));
    }

    @Override
    public void playerLeft(ServerPlayer $$0) {
        this.broadcastNotification(OutgoingRpcMethods.PLAYER_LEFT, PlayerDto.from($$0));
    }

    @Override
    public void serverStarted() {
        this.broadcastNotification(OutgoingRpcMethods.SERVER_STARTED);
    }

    @Override
    public void serverShuttingDown() {
        this.broadcastNotification(OutgoingRpcMethods.SERVER_SHUTTING_DOWN);
    }

    @Override
    public void serverSaveStarted() {
        this.broadcastNotification(OutgoingRpcMethods.SERVER_SAVE_STARTED);
    }

    @Override
    public void serverSaveCompleted() {
        this.broadcastNotification(OutgoingRpcMethods.SERVER_SAVE_COMPLETED);
    }

    @Override
    public void serverActivityOccured() {
        this.broadcastNotification(OutgoingRpcMethods.SERVER_ACTIVITY_OCCURRED);
    }

    @Override
    public void playerOped(ServerOpListEntry $$0) {
        this.broadcastNotification(OutgoingRpcMethods.PLAYER_OPED, OperatorService.OperatorDto.from($$0));
    }

    @Override
    public void playerDeoped(ServerOpListEntry $$0) {
        this.broadcastNotification(OutgoingRpcMethods.PLAYER_DEOPED, OperatorService.OperatorDto.from($$0));
    }

    @Override
    public void playerAddedToAllowlist(NameAndId $$0) {
        this.broadcastNotification(OutgoingRpcMethods.PLAYER_ADDED_TO_ALLOWLIST, PlayerDto.from($$0));
    }

    @Override
    public void playerRemovedFromAllowlist(NameAndId $$0) {
        this.broadcastNotification(OutgoingRpcMethods.PLAYER_REMOVED_FROM_ALLOWLIST, PlayerDto.from($$0));
    }

    @Override
    public void ipBanned(IpBanListEntry $$0) {
        this.broadcastNotification(OutgoingRpcMethods.IP_BANNED, IpBanlistService.IpBanDto.from($$0));
    }

    @Override
    public void ipUnbanned(String $$0) {
        this.broadcastNotification(OutgoingRpcMethods.IP_UNBANNED, $$0);
    }

    @Override
    public void playerBanned(UserBanListEntry $$0) {
        this.broadcastNotification(OutgoingRpcMethods.PLAYER_BANNED, BanlistService.UserBanDto.from($$0));
    }

    @Override
    public void playerUnbanned(NameAndId $$0) {
        this.broadcastNotification(OutgoingRpcMethods.PLAYER_UNBANNED, PlayerDto.from($$0));
    }

    @Override
    public <T> void onGameRuleChanged(GameRule<T> $$0, T $$1) {
        this.broadcastNotification(OutgoingRpcMethods.GAMERULE_CHANGED, GameRulesService.getTypedRule(this.minecraftApi, $$0, $$1));
    }

    @Override
    public void statusHeartbeat() {
        this.broadcastNotification(OutgoingRpcMethods.STATUS_HEARTBEAT, ServerStateService.status(this.minecraftApi));
    }

    private void broadcastNotification(Holder.Reference<? extends OutgoingRpcMethod<Void, ?>> $$0) {
        this.managementServer.forEachConnection($$1 -> $$1.sendNotification($$0));
    }

    private <Params> void broadcastNotification(Holder.Reference<? extends OutgoingRpcMethod<Params, ?>> $$0, Params $$1) {
        this.managementServer.forEachConnection($$2 -> $$2.sendNotification($$0, $$1));
    }
}

