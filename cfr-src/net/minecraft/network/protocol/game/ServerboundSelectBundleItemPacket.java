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

public record ServerboundSelectBundleItemPacket(int slotId, int selectedItemIndex) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundSelectBundleItemPacket> STREAM_CODEC = Packet.codec(ServerboundSelectBundleItemPacket::write, ServerboundSelectBundleItemPacket::new);

    private ServerboundSelectBundleItemPacket(FriendlyByteBuf $$0) {
        this($$0.readVarInt(), $$0.readVarInt());
        if (this.selectedItemIndex < 0 && this.selectedItemIndex != -1) {
            throw new IllegalArgumentException("Invalid selectedItemIndex: " + this.selectedItemIndex);
        }
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.slotId);
        $$0.writeVarInt(this.selectedItemIndex);
    }

    @Override
    public PacketType<ServerboundSelectBundleItemPacket> type() {
        return GamePacketTypes.SERVERBOUND_BUNDLE_ITEM_SELECTED;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleBundleItemSelectedPacket(this);
    }
}

