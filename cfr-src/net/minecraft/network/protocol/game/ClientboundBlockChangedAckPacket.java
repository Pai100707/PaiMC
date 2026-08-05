/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public record ClientboundBlockChangedAckPacket(int sequence) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundBlockChangedAckPacket> STREAM_CODEC = Packet.codec(ClientboundBlockChangedAckPacket::write, ClientboundBlockChangedAckPacket::new);

    private ClientboundBlockChangedAckPacket(FriendlyByteBuf $$0) {
        this($$0.readVarInt());
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.sequence);
    }

    @Override
    public PacketType<ClientboundBlockChangedAckPacket> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_CHANGED_ACK;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleBlockChangedAck(this);
    }
}

