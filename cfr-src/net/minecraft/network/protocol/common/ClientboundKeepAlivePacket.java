/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.CommonPacketTypes;

public class ClientboundKeepAlivePacket
implements Packet<ClientCommonPacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundKeepAlivePacket> STREAM_CODEC = Packet.codec(ClientboundKeepAlivePacket::write, ClientboundKeepAlivePacket::new);
    private final long id;

    public ClientboundKeepAlivePacket(long $$0) {
        this.id = $$0;
    }

    private ClientboundKeepAlivePacket(FriendlyByteBuf $$0) {
        this.id = $$0.readLong();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeLong(this.id);
    }

    @Override
    public PacketType<ClientboundKeepAlivePacket> type() {
        return CommonPacketTypes.CLIENTBOUND_KEEP_ALIVE;
    }

    @Override
    public void handle(ClientCommonPacketListener $$0) {
        $$0.handleKeepAlive(this);
    }

    public long getId() {
        return this.id;
    }
}

