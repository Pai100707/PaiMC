/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.LoginPacketTypes;

public class ClientboundLoginCompressionPacket
implements Packet<ClientLoginPacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundLoginCompressionPacket> STREAM_CODEC = Packet.codec(ClientboundLoginCompressionPacket::write, ClientboundLoginCompressionPacket::new);
    private final int compressionThreshold;

    public ClientboundLoginCompressionPacket(int $$0) {
        this.compressionThreshold = $$0;
    }

    private ClientboundLoginCompressionPacket(FriendlyByteBuf $$0) {
        this.compressionThreshold = $$0.readVarInt();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.compressionThreshold);
    }

    @Override
    public PacketType<ClientboundLoginCompressionPacket> type() {
        return LoginPacketTypes.CLIENTBOUND_LOGIN_COMPRESSION;
    }

    @Override
    public void handle(ClientLoginPacketListener $$0) {
        $$0.handleCompression(this);
    }

    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }
}

