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

public class ClientboundSetBorderSizePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetBorderSizePacket> STREAM_CODEC = Packet.codec(ClientboundSetBorderSizePacket::write, ClientboundSetBorderSizePacket::new);
    private final double size;

    public ClientboundSetBorderSizePacket(WorldBorder $$0) {
        this.size = $$0.getLerpTarget();
    }

    private ClientboundSetBorderSizePacket(FriendlyByteBuf $$0) {
        this.size = $$0.readDouble();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeDouble(this.size);
    }

    @Override
    public PacketType<ClientboundSetBorderSizePacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_BORDER_SIZE;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSetBorderSize(this);
    }

    public double getSize() {
        return this.size;
    }
}

