/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ClientboundSetEquipmentPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetEquipmentPacket> STREAM_CODEC = Packet.codec(ClientboundSetEquipmentPacket::write, ClientboundSetEquipmentPacket::new);
    private static final byte CONTINUE_MASK = -128;
    private final int entity;
    private final List<Pair<EquipmentSlot, ItemStack>> slots;

    public ClientboundSetEquipmentPacket(int $$0, List<Pair<EquipmentSlot, ItemStack>> $$1) {
        this.entity = $$0;
        this.slots = $$1;
    }

    private ClientboundSetEquipmentPacket(RegistryFriendlyByteBuf $$0) {
        byte $$1;
        this.entity = $$0.readVarInt();
        this.slots = Lists.newArrayList();
        do {
            $$1 = $$0.readByte();
            EquipmentSlot $$2 = EquipmentSlot.VALUES.get($$1 & 0x7F);
            ItemStack $$3 = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode($$0);
            this.slots.add((Pair<EquipmentSlot, ItemStack>)Pair.of((Object)$$2, (Object)$$3));
        } while (($$1 & 0xFFFFFF80) != 0);
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeVarInt(this.entity);
        int $$1 = this.slots.size();
        for (int $$2 = 0; $$2 < $$1; ++$$2) {
            Pair<EquipmentSlot, ItemStack> $$3 = this.slots.get($$2);
            EquipmentSlot $$4 = (EquipmentSlot)$$3.getFirst();
            boolean $$5 = $$2 != $$1 - 1;
            int $$6 = $$4.ordinal();
            $$0.writeByte($$5 ? $$6 | 0xFFFFFF80 : $$6);
            ItemStack.OPTIONAL_STREAM_CODEC.encode($$0, (ItemStack)$$3.getSecond());
        }
    }

    @Override
    public PacketType<ClientboundSetEquipmentPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_EQUIPMENT;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSetEquipment(this);
    }

    public int getEntity() {
        return this.entity;
    }

    public List<Pair<EquipmentSlot, ItemStack>> getSlots() {
        return this.slots;
    }
}

