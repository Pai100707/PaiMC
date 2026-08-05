/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ServerboundChunkBatchReceivedPacket(float desiredChunksPerTick) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundChunkBatchReceivedPacket> STREAM_CODEC = Packet.codec(ServerboundChunkBatchReceivedPacket::write, ServerboundChunkBatchReceivedPacket::new);

    private ServerboundChunkBatchReceivedPacket(FriendlyByteBuf $$0) {
        this($$0.readFloat());
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeFloat(this.desiredChunksPerTick);
    }

    @Override
    public PacketType<ServerboundChunkBatchReceivedPacket> type() {
        return GamePacketTypes.SERVERBOUND_CHUNK_BATCH_RECEIVED;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleChunkBatchReceived(this);
    }
}

