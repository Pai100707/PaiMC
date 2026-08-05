/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public class ClientboundRemoveEntitiesPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundRemoveEntitiesPacket> STREAM_CODEC = Packet.codec(ClientboundRemoveEntitiesPacket::write, ClientboundRemoveEntitiesPacket::new);
    private final IntList entityIds;

    public ClientboundRemoveEntitiesPacket(IntList $$0) {
        this.entityIds = new IntArrayList($$0);
    }

    public ClientboundRemoveEntitiesPacket(int ... $$0) {
        this.entityIds = new IntArrayList($$0);
    }

    private ClientboundRemoveEntitiesPacket(FriendlyByteBuf $$0) {
        this.entityIds = $$0.readIntIdList();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeIntIdList(this.entityIds);
    }

    @Override
    public PacketType<ClientboundRemoveEntitiesPacket> type() {
        return GamePacketTypes.CLIENTBOUND_REMOVE_ENTITIES;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleRemoveEntities(this);
    }

    public IntList getEntityIds() {
        return this.entityIds;
    }
}

