/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;

public class ServerHandshakePacketListenerImpl
implements ServerHandshakePacketListener {
    private static final Component IGNORE_STATUS_REASON = Component.translatable("disconnect.ignoring_status_request");
    private final MinecraftServer server;
    private final Connection connection;

    public ServerHandshakePacketListenerImpl(MinecraftServer $$0, Connection $$1) {
        this.server = $$0;
        this.connection = $$1;
    }

    @Override
    public void handleIntention(ClientIntentionPacket $$0) {
        switch ($$0.intention()) {
            case LOGIN: {
                this.beginLogin($$0, false);
                break;
            }
            case STATUS: {
                ServerStatus $$1 = this.server.getStatus();
                this.connection.setupOutboundProtocol(StatusProtocols.CLIENTBOUND);
                if (this.server.repliesToStatus() && $$1 != null) {
                    this.connection.setupInboundProtocol(StatusProtocols.SERVERBOUND, new ServerStatusPacketListenerImpl($$1, this.connection));
                    break;
                }
                this.connection.disconnect(IGNORE_STATUS_REASON);
                break;
            }
            case TRANSFER: {
                if (!this.server.acceptsTransfers()) {
                    this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
                    MutableComponent $$2 = Component.translatable("multiplayer.disconnect.transfers_disabled");
                    this.connection.send(new ClientboundLoginDisconnectPacket($$2));
                    this.connection.disconnect($$2);
                    break;
                }
                this.beginLogin($$0, true);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Invalid intention " + String.valueOf((Object)$$0.intention()));
            }
        }
    }

    private void beginLogin(ClientIntentionPacket $$0, boolean $$1) {
        this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
        if ($$0.protocolVersion() != SharedConstants.getCurrentVersion().protocolVersion()) {
            MutableComponent $$3;
            if ($$0.protocolVersion() < 754) {
                MutableComponent $$2 = Component.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getCurrentVersion().name());
            } else {
                $$3 = Component.translatable("multiplayer.disconnect.incompatible", SharedConstants.getCurrentVersion().name());
            }
            this.connection.send(new ClientboundLoginDisconnectPacket($$3));
            this.connection.disconnect($$3);
        } else {
            this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new ServerLoginPacketListenerImpl(this.server, this.connection, $$1));
        }
    }

    @Override
    public void onDisconnect(DisconnectionDetails $$0) {
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}

