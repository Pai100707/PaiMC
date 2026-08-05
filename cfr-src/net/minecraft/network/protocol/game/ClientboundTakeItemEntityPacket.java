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

public class ClientboundTakeItemEntityPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundTakeItemEntityPacket> STREAM_CODEC = Packet.codec(ClientboundTakeItemEntityPacket::write, ClientboundTakeItemEntityPacket::new);
    private final int itemId;
    private final int playerId;
    private final int amount;

    public ClientboundTakeItemEntityPacket(int $$0, int $$1, int $$2) {
        this.itemId = $$0;
        this.playerId = $$1;
        this.amount = $$2;
    }

    private ClientboundTakeItemEntityPacket(FriendlyByteBuf $$0) {
        this.itemId = $$0.readVarInt();
        this.playerId = $$0.readVarInt();
        this.amount = $$0.readVarInt();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.itemId);
        $$0.writeVarInt(this.playerId);
        $$0.writeVarInt(this.amount);
    }

    @Override
    public PacketType<ClientboundTakeItemEntityPacket> type() {
        return GamePacketTypes.CLIENTBOUND_TAKE_ITEM_ENTITY;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleTakeItemEntity(this);
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public int getAmount() {
        return this.amount;
    }
}

