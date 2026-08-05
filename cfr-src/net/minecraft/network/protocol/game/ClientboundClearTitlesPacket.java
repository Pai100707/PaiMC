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

public class ClientboundClearTitlesPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundClearTitlesPacket> STREAM_CODEC = Packet.codec(ClientboundClearTitlesPacket::write, ClientboundClearTitlesPacket::new);
    private final boolean resetTimes;

    public ClientboundClearTitlesPacket(boolean $$0) {
        this.resetTimes = $$0;
    }

    private ClientboundClearTitlesPacket(FriendlyByteBuf $$0) {
        this.resetTimes = $$0.readBoolean();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeBoolean(this.resetTimes);
    }

    @Override
    public PacketType<ClientboundClearTitlesPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CLEAR_TITLES;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleTitlesClear(this);
    }

    public boolean shouldResetTimes() {
        return this.resetTimes;
    }
}

