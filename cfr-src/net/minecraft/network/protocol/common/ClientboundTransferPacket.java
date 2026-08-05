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

public record ClientboundTransferPacket(String host, int port) implements Packet<ClientCommonPacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundTransferPacket> STREAM_CODEC = Packet.codec(ClientboundTransferPacket::write, ClientboundTransferPacket::new);

    private ClientboundTransferPacket(FriendlyByteBuf $$0) {
        this($$0.readUtf(), $$0.readVarInt());
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeUtf(this.host);
        $$0.writeVarInt(this.port);
    }

    @Override
    public PacketType<ClientboundTransferPacket> type() {
        return CommonPacketTypes.CLIENTBOUND_TRANSFER;
    }

    @Override
    public void handle(ClientCommonPacketListener $$0) {
        $$0.handleTransfer(this);
    }
}

