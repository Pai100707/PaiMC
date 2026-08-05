/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;

public class ServerStatusPacketListenerImpl
implements ServerStatusPacketListener {
    private static final Component DISCONNECT_REASON = Component.translatable("multiplayer.status.request_handled");
    private final ServerStatus status;
    private final Connection connection;
    private boolean hasRequestedStatus;

    public ServerStatusPacketListenerImpl(ServerStatus $$0, Connection $$1) {
        this.status = $$0;
        this.connection = $$1;
    }

    @Override
    public void onDisconnect(DisconnectionDetails $$0) {
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    @Override
    public void handleStatusRequest(ServerboundStatusRequestPacket $$0) {
        if (this.hasRequestedStatus) {
            this.connection.disconnect(DISCONNECT_REASON);
            return;
        }
        this.hasRequestedStatus = true;
        this.connection.send(new ClientboundStatusResponsePacket(this.status));
    }

    @Override
    public void handlePingRequest(ServerboundPingRequestPacket $$0) {
        this.connection.send(new ClientboundPongResponsePacket($$0.getTime()));
        this.connection.disconnect(DISCONNECT_REASON);
    }
}

