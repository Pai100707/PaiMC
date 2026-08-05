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

public class ServerboundEntityTagQueryPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundEntityTagQueryPacket> STREAM_CODEC = Packet.codec(ServerboundEntityTagQueryPacket::write, ServerboundEntityTagQueryPacket::new);
    private final int transactionId;
    private final int entityId;

    public ServerboundEntityTagQueryPacket(int $$0, int $$1) {
        this.transactionId = $$0;
        this.entityId = $$1;
    }

    private ServerboundEntityTagQueryPacket(FriendlyByteBuf $$0) {
        this.transactionId = $$0.readVarInt();
        this.entityId = $$0.readVarInt();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.transactionId);
        $$0.writeVarInt(this.entityId);
    }

    @Override
    public PacketType<ServerboundEntityTagQueryPacket> type() {
        return GamePacketTypes.SERVERBOUND_ENTITY_TAG_QUERY;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleEntityTagQuery(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public int getEntityId() {
        return this.entityId;
    }
}

