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

public record ServerboundContainerSlotStateChangedPacket(int slotId, int containerId, boolean newState) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundContainerSlotStateChangedPacket> STREAM_CODEC = Packet.codec(ServerboundContainerSlotStateChangedPacket::write, ServerboundContainerSlotStateChangedPacket::new);

    private ServerboundContainerSlotStateChangedPacket(FriendlyByteBuf $$0) {
        this($$0.readVarInt(), $$0.readContainerId(), $$0.readBoolean());
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.slotId);
        $$0.writeContainerId(this.containerId);
        $$0.writeBoolean(this.newState);
    }

    @Override
    public PacketType<ServerboundContainerSlotStateChangedPacket> type() {
        return GamePacketTypes.SERVERBOUND_CONTAINER_SLOT_STATE_CHANGED;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleContainerSlotStateChanged(this);
    }
}

