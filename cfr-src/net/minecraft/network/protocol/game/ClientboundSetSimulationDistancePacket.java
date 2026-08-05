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

public record ClientboundSetSimulationDistancePacket(int simulationDistance) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetSimulationDistancePacket> STREAM_CODEC = Packet.codec(ClientboundSetSimulationDistancePacket::write, ClientboundSetSimulationDistancePacket::new);

    private ClientboundSetSimulationDistancePacket(FriendlyByteBuf $$0) {
        this($$0.readVarInt());
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.simulationDistance);
    }

    @Override
    public PacketType<ClientboundSetSimulationDistancePacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_SIMULATION_DISTANCE;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSetSimulationDistance(this);
    }
}

