/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.internalapi;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.internalapi.MinecraftAllowListService;
import net.minecraft.server.jsonrpc.internalapi.MinecraftAllowListServiceImpl;
import net.minecraft.server.jsonrpc.internalapi.MinecraftBanListService;
import net.minecraft.server.jsonrpc.internalapi.MinecraftBanListServiceImpl;
import net.minecraft.server.jsonrpc.internalapi.MinecraftExecutorService;
import net.minecraft.server.jsonrpc.internalapi.MinecraftExecutorServiceImpl;
import net.minecraft.server.jsonrpc.internalapi.MinecraftGameRuleService;
import net.minecraft.server.jsonrpc.internalapi.MinecraftGameRuleServiceImpl;
import net.minecraft.server.jsonrpc.internalapi.MinecraftOperatorListService;
import net.minecraft.server.jsonrpc.internalapi.MinecraftOperatorListServiceImpl;
import net.minecraft.server.jsonrpc.internalapi.MinecraftPlayerListService;
import net.minecraft.server.jsonrpc.internalapi.MinecraftPlayerListServiceImpl;
import net.minecraft.server.jsonrpc.internalapi.MinecraftServerSettingsService;
import net.minecraft.server.jsonrpc.internalapi.MinecraftServerSettingsServiceImpl;
import net.minecraft.server.jsonrpc.internalapi.MinecraftServerStateService;
import net.minecraft.server.jsonrpc.internalapi.MinecraftServerStateServiceImpl;
import net.minecraft.server.notifications.NotificationManager;

public class MinecraftApi {
    private final NotificationManager notificationManager;
    private final MinecraftAllowListService allowListService;
    private final MinecraftBanListService banListService;
    private final MinecraftPlayerListService minecraftPlayerListService;
    private final MinecraftGameRuleService gameRuleService;
    private final MinecraftOperatorListService minecraftOperatorListService;
    private final MinecraftServerSettingsService minecraftServerSettingsService;
    private final MinecraftServerStateService minecraftServerStateService;
    private final MinecraftExecutorService executorService;

    public MinecraftApi(NotificationManager $$0, MinecraftAllowListService $$1, MinecraftBanListService $$2, MinecraftPlayerListService $$3, MinecraftGameRuleService $$4, MinecraftOperatorListService $$5, MinecraftServerSettingsService $$6, MinecraftServerStateService $$7, MinecraftExecutorService $$8) {
        this.notificationManager = $$0;
        this.allowListService = $$1;
        this.banListService = $$2;
        this.minecraftPlayerListService = $$3;
        this.gameRuleService = $$4;
        this.minecraftOperatorListService = $$5;
        this.minecraftServerSettingsService = $$6;
        this.minecraftServerStateService = $$7;
        this.executorService = $$8;
    }

    public <V> CompletableFuture<V> submit(Supplier<V> $$0) {
        return this.executorService.submit($$0);
    }

    public CompletableFuture<Void> submit(Runnable $$0) {
        return this.executorService.submit($$0);
    }

    public MinecraftAllowListService allowListService() {
        return this.allowListService;
    }

    public MinecraftBanListService banListService() {
        return this.banListService;
    }

    public MinecraftPlayerListService playerListService() {
        return this.minecraftPlayerListService;
    }

    public MinecraftGameRuleService gameRuleService() {
        return this.gameRuleService;
    }

    public MinecraftOperatorListService operatorListService() {
        return this.minecraftOperatorListService;
    }

    public MinecraftServerSettingsService serverSettingsService() {
        return this.minecraftServerSettingsService;
    }

    public MinecraftServerStateService serverStateService() {
        return this.minecraftServerStateService;
    }

    public NotificationManager notificationManager() {
        return this.notificationManager;
    }

    public static MinecraftApi of(DedicatedServer $$0) {
        JsonRpcLogger $$1 = new JsonRpcLogger();
        MinecraftAllowListServiceImpl $$2 = new MinecraftAllowListServiceImpl($$0, $$1);
        MinecraftBanListServiceImpl $$3 = new MinecraftBanListServiceImpl($$0, $$1);
        MinecraftPlayerListServiceImpl $$4 = new MinecraftPlayerListServiceImpl($$0, $$1);
        MinecraftGameRuleServiceImpl $$5 = new MinecraftGameRuleServiceImpl($$0, $$1);
        MinecraftOperatorListServiceImpl $$6 = new MinecraftOperatorListServiceImpl($$0, $$1);
        MinecraftServerSettingsServiceImpl $$7 = new MinecraftServerSettingsServiceImpl($$0, $$1);
        MinecraftServerStateServiceImpl $$8 = new MinecraftServerStateServiceImpl($$0, $$1);
        MinecraftExecutorServiceImpl $$9 = new MinecraftExecutorServiceImpl($$0);
        return new MinecraftApi($$0.notificationManager(), $$2, $$3, $$4, $$5, $$6, $$7, $$8, $$9);
    }
}

