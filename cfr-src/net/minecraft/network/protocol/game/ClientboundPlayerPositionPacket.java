/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;

public record ClientboundPlayerPositionPacket(int id, PositionMoveRotation change, Set<Relative> relatives) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundPlayerPositionPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientboundPlayerPositionPacket::id, PositionMoveRotation.STREAM_CODEC, ClientboundPlayerPositionPacket::change, Relative.SET_STREAM_CODEC, ClientboundPlayerPositionPacket::relatives, ClientboundPlayerPositionPacket::new);

    public static ClientboundPlayerPositionPacket of(int $$0, PositionMoveRotation $$1, Set<Relative> $$2) {
        return new ClientboundPlayerPositionPacket($$0, $$1, $$2);
    }

    @Override
    public PacketType<ClientboundPlayerPositionPacket> type() {
        return GamePacketTypes.CLIENTBOUND_PLAYER_POSITION;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleMovePlayer(this);
    }
}

