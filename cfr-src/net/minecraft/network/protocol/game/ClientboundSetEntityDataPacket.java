/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.syncher.SynchedEntityData;

public record ClientboundSetEntityDataPacket(int id, List<SynchedEntityData.DataValue<?>> packedItems) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetEntityDataPacket> STREAM_CODEC = Packet.codec(ClientboundSetEntityDataPacket::write, ClientboundSetEntityDataPacket::new);
    public static final int EOF_MARKER = 255;

    private ClientboundSetEntityDataPacket(RegistryFriendlyByteBuf $$0) {
        this($$0.readVarInt(), ClientboundSetEntityDataPacket.unpack($$0));
    }

    private static void pack(List<SynchedEntityData.DataValue<?>> $$0, RegistryFriendlyByteBuf $$1) {
        for (SynchedEntityData.DataValue<?> $$2 : $$0) {
            $$2.write($$1);
        }
        $$1.writeByte(255);
    }

    private static List<SynchedEntityData.DataValue<?>> unpack(RegistryFriendlyByteBuf $$0) {
        short $$2;
        ArrayList $$1 = new ArrayList();
        while (($$2 = $$0.readUnsignedByte()) != 255) {
            $$1.add(SynchedEntityData.DataValue.read($$0, $$2));
        }
        return $$1;
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeVarInt(this.id);
        ClientboundSetEntityDataPacket.pack(this.packedItems, $$0);
    }

    @Override
    public PacketType<ClientboundSetEntityDataPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_ENTITY_DATA;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSetEntityData(this);
    }
}

