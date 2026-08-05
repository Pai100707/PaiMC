/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.internalapi;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.internalapi.MinecraftServerSettingsService;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

public class MinecraftServerSettingsServiceImpl
implements MinecraftServerSettingsService {
    private final DedicatedServer server;
    private final JsonRpcLogger jsonrpcLogger;

    public MinecraftServerSettingsServiceImpl(DedicatedServer $$0, JsonRpcLogger $$1) {
        this.server = $$0;
        this.jsonrpcLogger = $$1;
    }

    @Override
    public boolean isAutoSave() {
        return this.server.isAutoSave();
    }

    @Override
    public boolean setAutoSave(boolean $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update autosave from {} to {}", this.isAutoSave(), $$0);
        this.server.setAutoSave($$0);
        return this.isAutoSave();
    }

    @Override
    public Difficulty getDifficulty() {
        return this.server.getWorldData().getDifficulty();
    }

    @Override
    public Difficulty setDifficulty(Difficulty $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update difficulty from '{}' to '{}'", this.getDifficulty(), $$0);
        this.server.setDifficulty($$0);
        return this.getDifficulty();
    }

    @Override
    public boolean isEnforceWhitelist() {
        return this.server.isEnforceWhitelist();
    }

    @Override
    public boolean setEnforceWhitelist(boolean $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update enforce allowlist from {} to {}", this.isEnforceWhitelist(), $$0);
        this.server.setEnforceWhitelist($$0);
        this.server.kickUnlistedPlayers();
        return this.isEnforceWhitelist();
    }

    @Override
    public boolean isUsingWhitelist() {
        return this.server.isUsingWhitelist();
    }

    @Override
    public boolean setUsingWhitelist(boolean $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update using allowlist from {} to {}", this.isUsingWhitelist(), $$0);
        this.server.setUsingWhitelist($$0);
        this.server.kickUnlistedPlayers();
        return this.isUsingWhitelist();
    }

    @Override
    public int getMaxPlayers() {
        return this.server.getMaxPlayers();
    }

    @Override
    public int setMaxPlayers(int $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update max players from {} to {}", this.getMaxPlayers(), $$0);
        this.server.setMaxPlayers($$0);
        return this.getMaxPlayers();
    }

    @Override
    public int getPauseWhenEmptySeconds() {
        return this.server.pauseWhenEmptySeconds();
    }

    @Override
    public int setPauseWhenEmptySeconds(int $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update pause when empty from {} seconds to {} seconds", this.getPauseWhenEmptySeconds(), $$0);
        this.server.setPauseWhenEmptySeconds($$0);
        return this.getPauseWhenEmptySeconds();
    }

    @Override
    public int getPlayerIdleTimeout() {
        return this.server.playerIdleTimeout();
    }

    @Override
    public int setPlayerIdleTimeout(int $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update player idle timeout from {} minutes to {} minutes", this.getPlayerIdleTimeout(), $$0);
        this.server.setPlayerIdleTimeout($$0);
        return this.getPlayerIdleTimeout();
    }

    @Override
    public boolean allowFlight() {
        return this.server.allowFlight();
    }

    @Override
    public boolean setAllowFlight(boolean $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update allow flight from {} to {}", this.allowFlight(), $$0);
        this.server.setAllowFlight($$0);
        return this.allowFlight();
    }

    @Override
    public int getSpawnProtectionRadius() {
        return this.server.spawnProtectionRadius();
    }

    @Override
    public int setSpawnProtectionRadius(int $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update spawn protection radius from {} to {}", this.getSpawnProtectionRadius(), $$0);
        this.server.setSpawnProtectionRadius($$0);
        return this.getSpawnProtectionRadius();
    }

    @Override
    public String getMotd() {
        return this.server.getMotd();
    }

    @Override
    public String setMotd(String $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update MOTD from '{}' to '{}'", this.getMotd(), $$0);
        this.server.setMotd($$0);
        return this.getMotd();
    }

    @Override
    public boolean forceGameMode() {
        return this.server.forceGameMode();
    }

    @Override
    public boolean setForceGameMode(boolean $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update force game mode from {} to {}", this.forceGameMode(), $$0);
        this.server.setForceGameMode($$0);
        return this.forceGameMode();
    }

    @Override
    public GameType getGameMode() {
        return this.server.gameMode();
    }

    @Override
    public GameType setGameMode(GameType $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update game mode from '{}' to '{}'", this.getGameMode(), $$0);
        this.server.setGameMode($$0);
        return this.getGameMode();
    }

    @Override
    public int getViewDistance() {
        return this.server.viewDistance();
    }

    @Override
    public int setViewDistance(int $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update view distance from {} to {}", this.getViewDistance(), $$0);
        this.server.setViewDistance($$0);
        return this.getViewDistance();
    }

    @Override
    public int getSimulationDistance() {
        return this.server.simulationDistance();
    }

    @Override
    public int setSimulationDistance(int $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update simulation distance from {} to {}", this.getSimulationDistance(), $$0);
        this.server.setSimulationDistance($$0);
        return this.getSimulationDistance();
    }

    @Override
    public boolean acceptsTransfers() {
        return this.server.acceptsTransfers();
    }

    @Override
    public boolean setAcceptsTransfers(boolean $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update accepts transfers from {} to {}", this.acceptsTransfers(), $$0);
        this.server.setAcceptsTransfers($$0);
        return this.acceptsTransfers();
    }

    @Override
    public int getStatusHeartbeatInterval() {
        return this.server.statusHeartbeatInterval();
    }

    @Override
    public int setStatusHeartbeatInterval(int $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update status heartbeat interval from {} to {}", this.getStatusHeartbeatInterval(), $$0);
        this.server.setStatusHeartbeatInterval($$0);
        return this.getStatusHeartbeatInterval();
    }

    @Override
    public LevelBasedPermissionSet getOperatorUserPermissions() {
        return this.server.operatorUserPermissions();
    }

    @Override
    public LevelBasedPermissionSet setOperatorUserPermissions(LevelBasedPermissionSet $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update operator user permission level from {} to {}", this.getOperatorUserPermissions(), $$0.level());
        this.server.setOperatorUserPermissions($$0);
        return this.getOperatorUserPermissions();
    }

    @Override
    public boolean hidesOnlinePlayers() {
        return this.server.hidesOnlinePlayers();
    }

    @Override
    public boolean setHidesOnlinePlayers(boolean $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update hides online players from {} to {}", this.hidesOnlinePlayers(), $$0);
        this.server.setHidesOnlinePlayers($$0);
        return this.hidesOnlinePlayers();
    }

    @Override
    public boolean repliesToStatus() {
        return this.server.repliesToStatus();
    }

    @Override
    public boolean setRepliesToStatus(boolean $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update replies to status from {} to {}", this.repliesToStatus(), $$0);
        this.server.setRepliesToStatus($$0);
        return this.repliesToStatus();
    }

    @Override
    public int getEntityBroadcastRangePercentage() {
        return this.server.entityBroadcastRangePercentage();
    }

    @Override
    public int setEntityBroadcastRangePercentage(int $$0, ClientInfo $$1) {
        this.jsonrpcLogger.log($$1, "Update entity broadcast range percentage from {}% to {}%", this.getEntityBroadcastRangePercentage(), $$0);
        this.server.setEntityBroadcastRangePercentage($$0);
        return this.getEntityBroadcastRangePercentage();
    }
}

