/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;

public class ServerboundPongPacket
implements Packet<ServerCommonPacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundPongPacket> STREAM_CODEC = Packet.codec(ServerboundPongPacket::write, ServerboundPongPacket::new);
    private final int id;

    public ServerboundPongPacket(int $$0) {
        this.id = $$0;
    }

    private ServerboundPongPacket(FriendlyByteBuf $$0) {
        this.id = $$0.readInt();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeInt(this.id);
    }

    @Override
    public PacketType<ServerboundPongPacket> type() {
        return CommonPacketTypes.SERVERBOUND_PONG;
    }

    @Override
    public void handle(ServerCommonPacketListener $$0) {
        $$0.handlePong(this);
    }

    public int getId() {
        return this.id;
    }
}

