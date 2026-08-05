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

public class ClientboundSetBorderWarningDelayPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetBorderWarningDelayPacket> STREAM_CODEC = Packet.codec(ClientboundSetBorderWarningDelayPacket::write, ClientboundSetBorderWarningDelayPacket::new);
    private final int warningDelay;

    public ClientboundSetBorderWarningDelayPacket(WorldBorder $$0) {
        this.warningDelay = $$0.getWarningTime();
    }

    private ClientboundSetBorderWarningDelayPacket(FriendlyByteBuf $$0) {
        this.warningDelay = $$0.readVarInt();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.warningDelay);
    }

    @Override
    public PacketType<ClientboundSetBorderWarningDelayPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_BORDER_WARNING_DELAY;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSetBorderWarningDelay(this);
    }

    public int getWarningDelay() {
        return this.warningDelay;
    }
}

