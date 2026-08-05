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
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderCenterPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetBorderCenterPacket> STREAM_CODEC = Packet.codec(ClientboundSetBorderCenterPacket::write, ClientboundSetBorderCenterPacket::new);
    private final double newCenterX;
    private final double newCenterZ;

    public ClientboundSetBorderCenterPacket(WorldBorder $$0) {
        this.newCenterX = $$0.getCenterX();
        this.newCenterZ = $$0.getCenterZ();
    }

    private ClientboundSetBorderCenterPacket(FriendlyByteBuf $$0) {
        this.newCenterX = $$0.readDouble();
        this.newCenterZ = $$0.readDouble();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeDouble(this.newCenterX);
        $$0.writeDouble(this.newCenterZ);
    }

    @Override
    public PacketType<ClientboundSetBorderCenterPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_BORDER_CENTER;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSetBorderCenter(this);
    }

    public double getNewCenterZ() {
        return this.newCenterZ;
    }

    public double getNewCenterX() {
        return this.newCenterX;
    }
}

