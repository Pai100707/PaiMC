/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetSlotPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundContainerSetSlotPacket> STREAM_CODEC = Packet.codec(ClientboundContainerSetSlotPacket::write, ClientboundContainerSetSlotPacket::new);
    private final int containerId;
    private final int stateId;
    private final int slot;
    private final ItemStack itemStack;

    public ClientboundContainerSetSlotPacket(int $$0, int $$1, int $$2, ItemStack $$3) {
        this.containerId = $$0;
        this.stateId = $$1;
        this.slot = $$2;
        this.itemStack = $$3.copy();
    }

    private ClientboundContainerSetSlotPacket(RegistryFriendlyByteBuf $$0) {
        this.containerId = $$0.readContainerId();
        this.stateId = $$0.readVarInt();
        this.slot = $$0.readShort();
        this.itemStack = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode($$0);
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeContainerId(this.containerId);
        $$0.writeVarInt(this.stateId);
        $$0.writeShort(this.slot);
        ItemStack.OPTIONAL_STREAM_CODEC.encode($$0, this.itemStack);
    }

    @Override
    public PacketType<ClientboundContainerSetSlotPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_SLOT;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleContainerSetSlot(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlot() {
        return this.slot;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    public int getStateId() {
        return this.stateId;
    }
}

