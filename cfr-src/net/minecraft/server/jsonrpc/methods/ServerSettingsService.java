/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.methods;

import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

public class ServerSettingsService {
    public static boolean autosave(MinecraftApi $$0) {
        return $$0.serverSettingsService().isAutoSave();
    }

    public static boolean setAutosave(MinecraftApi $$0, boolean $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setAutoSave($$1, $$2);
    }

    public static Difficulty difficulty(MinecraftApi $$0) {
        return $$0.serverSettingsService().getDifficulty();
    }

    public static Difficulty setDifficulty(MinecraftApi $$0, Difficulty $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setDifficulty($$1, $$2);
    }

    public static boolean enforceAllowlist(MinecraftApi $$0) {
        return $$0.serverSettingsService().isEnforceWhitelist();
    }

    public static boolean setEnforceAllowlist(MinecraftApi $$0, boolean $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setEnforceWhitelist($$1, $$2);
    }

    public static boolean usingAllowlist(MinecraftApi $$0) {
        return $$0.serverSettingsService().isUsingWhitelist();
    }

    public static boolean setUsingAllowlist(MinecraftApi $$0, boolean $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setUsingWhitelist($$1, $$2);
    }

    public static int maxPlayers(MinecraftApi $$0) {
        return $$0.serverSettingsService().getMaxPlayers();
    }

    public static int setMaxPlayers(MinecraftApi $$0, int $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setMaxPlayers($$1, $$2);
    }

    public static int pauseWhenEmpty(MinecraftApi $$0) {
        return $$0.serverSettingsService().getPauseWhenEmptySeconds();
    }

    public static int setPauseWhenEmpty(MinecraftApi $$0, int $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setPauseWhenEmptySeconds($$1, $$2);
    }

    public static int playerIdleTimeout(MinecraftApi $$0) {
        return $$0.serverSettingsService().getPlayerIdleTimeout();
    }

    public static int setPlayerIdleTimeout(MinecraftApi $$0, int $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setPlayerIdleTimeout($$1, $$2);
    }

    public static boolean allowFlight(MinecraftApi $$0) {
        return $$0.serverSettingsService().allowFlight();
    }

    public static boolean setAllowFlight(MinecraftApi $$0, boolean $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setAllowFlight($$1, $$2);
    }

    public static int spawnProtection(MinecraftApi $$0) {
        return $$0.serverSettingsService().getSpawnProtectionRadius();
    }

    public static int setSpawnProtection(MinecraftApi $$0, int $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setSpawnProtectionRadius($$1, $$2);
    }

    public static String motd(MinecraftApi $$0) {
        return $$0.serverSettingsService().getMotd();
    }

    public static String setMotd(MinecraftApi $$0, String $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setMotd($$1, $$2);
    }

    public static boolean forceGameMode(MinecraftApi $$0) {
        return $$0.serverSettingsService().forceGameMode();
    }

    public static boolean setForceGameMode(MinecraftApi $$0, boolean $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setForceGameMode($$1, $$2);
    }

    public static GameType gameMode(MinecraftApi $$0) {
        return $$0.serverSettingsService().getGameMode();
    }

    public static GameType setGameMode(MinecraftApi $$0, GameType $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setGameMode($$1, $$2);
    }

    public static int viewDistance(MinecraftApi $$0) {
        return $$0.serverSettingsService().getViewDistance();
    }

    public static int setViewDistance(MinecraftApi $$0, int $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setViewDistance($$1, $$2);
    }

    public static int simulationDistance(MinecraftApi $$0) {
        return $$0.serverSettingsService().getSimulationDistance();
    }

    public static int setSimulationDistance(MinecraftApi $$0, int $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setSimulationDistance($$1, $$2);
    }

    public static boolean acceptTransfers(MinecraftApi $$0) {
        return $$0.serverSettingsService().acceptsTransfers();
    }

    public static boolean setAcceptTransfers(MinecraftApi $$0, boolean $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setAcceptsTransfers($$1, $$2);
    }

    public static int statusHeartbeatInterval(MinecraftApi $$0) {
        return $$0.serverSettingsService().getStatusHeartbeatInterval();
    }

    public static int setStatusHeartbeatInterval(MinecraftApi $$0, int $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setStatusHeartbeatInterval($$1, $$2);
    }

    public static PermissionLevel operatorUserPermissionLevel(MinecraftApi $$0) {
        return $$0.serverSettingsService().getOperatorUserPermissions().level();
    }

    public static PermissionLevel setOperatorUserPermissionLevel(MinecraftApi $$0, PermissionLevel $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setOperatorUserPermissions(LevelBasedPermissionSet.forLevel($$1), $$2).level();
    }

    public static boolean hidesOnlinePlayers(MinecraftApi $$0) {
        return $$0.serverSettingsService().hidesOnlinePlayers();
    }

    public static boolean setHidesOnlinePlayers(MinecraftApi $$0, boolean $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setHidesOnlinePlayers($$1, $$2);
    }

    public static boolean repliesToStatus(MinecraftApi $$0) {
        return $$0.serverSettingsService().repliesToStatus();
    }

    public static boolean setRepliesToStatus(MinecraftApi $$0, boolean $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setRepliesToStatus($$1, $$2);
    }

    public static int entityBroadcastRangePercentage(MinecraftApi $$0) {
        return $$0.serverSettingsService().getEntityBroadcastRangePercentage();
    }

    public static int setEntityBroadcastRangePercentage(MinecraftApi $$0, int $$1, ClientInfo $$2) {
        return $$0.serverSettingsService().setEntityBroadcastRangePercentage($$1, $$2);
    }
}

