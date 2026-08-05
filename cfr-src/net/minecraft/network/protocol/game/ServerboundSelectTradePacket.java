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

public class ServerboundSelectTradePacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundSelectTradePacket> STREAM_CODEC = Packet.codec(ServerboundSelectTradePacket::write, ServerboundSelectTradePacket::new);
    private final int item;

    public ServerboundSelectTradePacket(int $$0) {
        this.item = $$0;
    }

    private ServerboundSelectTradePacket(FriendlyByteBuf $$0) {
        this.item = $$0.readVarInt();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.item);
    }

    @Override
    public PacketType<ServerboundSelectTradePacket> type() {
        return GamePacketTypes.SERVERBOUND_SELECT_TRADE;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleSelectTrade(this);
    }

    public int getItem() {
        return this.item;
    }
}

