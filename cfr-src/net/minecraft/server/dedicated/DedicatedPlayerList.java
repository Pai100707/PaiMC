/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.slf4j.Logger;

public class DedicatedPlayerList
extends PlayerList {
    private static final Logger LOGGER = LogUtils.getLogger();

    public DedicatedPlayerList(DedicatedServer $$0, LayeredRegistryAccess<RegistryLayer> $$1, PlayerDataStorage $$2) {
        super($$0, $$1, $$2, $$0.notificationManager());
        this.setViewDistance($$0.viewDistance());
        this.setSimulationDistance($$0.simulationDistance());
        this.loadUserBanList();
        this.saveUserBanList();
        this.loadIpBanList();
        this.saveIpBanList();
        this.loadOps();
        this.loadWhiteList();
        this.saveOps();
        if (!this.getWhiteList().getFile().exists()) {
            this.saveWhiteList();
        }
    }

    @Override
    public void reloadWhiteList() {
        this.loadWhiteList();
    }

    private void saveIpBanList() {
        try {
            this.getIpBans().save();
        }
        catch (IOException $$0) {
            LOGGER.warn("Failed to save ip banlist: ", (Throwable)$$0);
        }
    }

    private void saveUserBanList() {
        try {
            this.getBans().save();
        }
        catch (IOException $$0) {
            LOGGER.warn("Failed to save user banlist: ", (Throwable)$$0);
        }
    }

    private void loadIpBanList() {
        try {
            this.getIpBans().load();
        }
        catch (IOException $$0) {
            LOGGER.warn("Failed to load ip banlist: ", (Throwable)$$0);
        }
    }

    private void loadUserBanList() {
        try {
            this.getBans().load();
        }
        catch (IOException $$0) {
            LOGGER.warn("Failed to load user banlist: ", (Throwable)$$0);
        }
    }

    private void loadOps() {
        try {
            this.getOps().load();
        }
        catch (Exception $$0) {
            LOGGER.warn("Failed to load operators list: ", (Throwable)$$0);
        }
    }

    private void saveOps() {
        try {
            this.getOps().save();
        }
        catch (Exception $$0) {
            LOGGER.warn("Failed to save operators list: ", (Throwable)$$0);
        }
    }

    private void loadWhiteList() {
        try {
            this.getWhiteList().load();
        }
        catch (Exception $$0) {
            LOGGER.warn("Failed to load white-list: ", (Throwable)$$0);
        }
    }

    private void saveWhiteList() {
        try {
            this.getWhiteList().save();
        }
        catch (Exception $$0) {
            LOGGER.warn("Failed to save white-list: ", (Throwable)$$0);
        }
    }

    @Override
    public boolean isWhiteListed(NameAndId $$0) {
        return !this.isUsingWhitelist() || this.isOp($$0) || this.getWhiteList().isWhiteListed($$0);
    }

    @Override
    public DedicatedServer getServer() {
        return (DedicatedServer)super.getServer();
    }

    @Override
    public boolean canBypassPlayerLimit(NameAndId $$0) {
        return this.getOps().canBypassPlayerLimit($$0);
    }

    @Override
    public /* synthetic */ MinecraftServer getServer() {
        return this.getServer();
    }
}

