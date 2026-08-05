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

public class ServerboundPaddleBoatPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundPaddleBoatPacket> STREAM_CODEC = Packet.codec(ServerboundPaddleBoatPacket::write, ServerboundPaddleBoatPacket::new);
    private final boolean left;
    private final boolean right;

    public ServerboundPaddleBoatPacket(boolean $$0, boolean $$1) {
        this.left = $$0;
        this.right = $$1;
    }

    private ServerboundPaddleBoatPacket(FriendlyByteBuf $$0) {
        this.left = $$0.readBoolean();
        this.right = $$0.readBoolean();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeBoolean(this.left);
        $$0.writeBoolean(this.right);
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handlePaddleBoat(this);
    }

    @Override
    public PacketType<ServerboundPaddleBoatPacket> type() {
        return GamePacketTypes.SERVERBOUND_PADDLE_BOAT;
    }

    public boolean getLeft() {
        return this.left;
    }

    public boolean getRight() {
        return this.right;
    }
}

