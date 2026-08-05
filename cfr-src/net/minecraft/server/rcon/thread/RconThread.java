/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.rcon.thread;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.rcon.thread.GenericThread;
import net.minecraft.server.rcon.thread.RconClient;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class RconThread
extends GenericThread {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerSocket socket;
    private final String rconPassword;
    private final List<RconClient> clients = Lists.newArrayList();
    private final ServerInterface serverInterface;

    private RconThread(ServerInterface $$0, ServerSocket $$1, String $$2) {
        super("RCON Listener");
        this.serverInterface = $$0;
        this.socket = $$1;
        this.rconPassword = $$2;
    }

    private void clearClients() {
        this.clients.removeIf($$0 -> !$$0.isRunning());
    }

    @Override
    public void run() {
        try {
            while (this.running) {
                try {
                    Socket $$0 = this.socket.accept();
                    RconClient $$1 = new RconClient(this.serverInterface, this.rconPassword, $$0);
                    $$1.start();
                    this.clients.add($$1);
                    this.clearClients();
                }
                catch (SocketTimeoutException $$2) {
                    this.clearClients();
                }
                catch (IOException $$3) {
                    if (!this.running) continue;
                    LOGGER.info("IO exception: ", (Throwable)$$3);
                }
            }
        }
        finally {
            this.closeSocket(this.socket);
        }
    }

    public static @Nullable RconThread create(ServerInterface $$0) {
        int $$3;
        DedicatedServerProperties $$1 = $$0.getProperties();
        String $$2 = $$0.getServerIp();
        if ($$2.isEmpty()) {
            $$2 = "0.0.0.0";
        }
        if (0 >= ($$3 = $$1.rconPort) || 65535 < $$3) {
            LOGGER.warn("Invalid rcon port {} found in server.properties, rcon disabled!", (Object)$$3);
            return null;
        }
        String $$4 = $$1.rconPassword;
        if ($$4.isEmpty()) {
            LOGGER.warn("No rcon password set in server.properties, rcon disabled!");
            return null;
        }
        try {
            ServerSocket $$5 = new ServerSocket($$3, 0, InetAddress.getByName($$2));
            $$5.setSoTimeout(500);
            RconThread $$6 = new RconThread($$0, $$5, $$4);
            if (!$$6.start()) {
                return null;
            }
            LOGGER.info("RCON running on {}:{}", (Object)$$2, (Object)$$3);
            return $$6;
        }
        catch (IOException $$7) {
            LOGGER.warn("Unable to initialise RCON on {}:{}", new Object[]{$$2, $$3, $$7});
            return null;
        }
    }

    @Override
    public void stop() {
        this.running = false;
        this.closeSocket(this.socket);
        super.stop();
        for (RconClient $$0 : this.clients) {
            if (!$$0.isRunning()) continue;
            $$0.stop();
        }
        this.clients.clear();
    }

    private void closeSocket(ServerSocket $$0) {
        LOGGER.debug("closeSocket: {}", (Object)$$0);
        try {
            $$0.close();
        }
        catch (IOException $$1) {
            LOGGER.warn("Failed to close socket", (Throwable)$$1);
        }
    }
}

