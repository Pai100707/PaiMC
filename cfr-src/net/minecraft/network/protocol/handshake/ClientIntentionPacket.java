/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.handshake;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.HandshakePacketTypes;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;

public record ClientIntentionPacket(int protocolVersion, String hostName, int port, ClientIntent intention) implements Packet<ServerHandshakePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientIntentionPacket> STREAM_CODEC = Packet.codec(ClientIntentionPacket::write, ClientIntentionPacket::new);
    private static final int MAX_HOST_LENGTH = 255;

    private ClientIntentionPacket(FriendlyByteBuf $$0) {
        this($$0.readVarInt(), $$0.readUtf(255), $$0.readUnsignedShort(), ClientIntent.byId($$0.readVarInt()));
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.protocolVersion);
        $$0.writeUtf(this.hostName);
        $$0.writeShort(this.port);
        $$0.writeVarInt(this.intention.id());
    }

    @Override
    public PacketType<ClientIntentionPacket> type() {
        return HandshakePacketTypes.CLIENT_INTENTION;
    }

    @Override
    public void handle(ServerHandshakePacketListener $$0) {
        $$0.handleIntention(this);
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}

