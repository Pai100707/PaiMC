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

public class ClientboundSetBorderWarningDistancePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetBorderWarningDistancePacket> STREAM_CODEC = Packet.codec(ClientboundSetBorderWarningDistancePacket::write, ClientboundSetBorderWarningDistancePacket::new);
    private final int warningBlocks;

    public ClientboundSetBorderWarningDistancePacket(WorldBorder $$0) {
        this.warningBlocks = $$0.getWarningBlocks();
    }

    private ClientboundSetBorderWarningDistancePacket(FriendlyByteBuf $$0) {
        this.warningBlocks = $$0.readVarInt();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.warningBlocks);
    }

    @Override
    public PacketType<ClientboundSetBorderWarningDistancePacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_BORDER_WARNING_DISTANCE;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSetBorderWarningDistance(this);
    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }
}

