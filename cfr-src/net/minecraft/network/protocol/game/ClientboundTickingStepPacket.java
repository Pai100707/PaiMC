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
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStepPacket(int tickSteps) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundTickingStepPacket> STREAM_CODEC = Packet.codec(ClientboundTickingStepPacket::write, ClientboundTickingStepPacket::new);

    private ClientboundTickingStepPacket(FriendlyByteBuf $$0) {
        this($$0.readVarInt());
    }

    public static ClientboundTickingStepPacket from(TickRateManager $$0) {
        return new ClientboundTickingStepPacket($$0.frozenTicksToRun());
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.tickSteps);
    }

    @Override
    public PacketType<ClientboundTickingStepPacket> type() {
        return GamePacketTypes.CLIENTBOUND_TICKING_STEP;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleTickingStep(this);
    }
}

