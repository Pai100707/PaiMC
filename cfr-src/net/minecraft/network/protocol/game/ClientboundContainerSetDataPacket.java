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

public class ClientboundContainerSetDataPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundContainerSetDataPacket> STREAM_CODEC = Packet.codec(ClientboundContainerSetDataPacket::write, ClientboundContainerSetDataPacket::new);
    private final int containerId;
    private final int id;
    private final int value;

    public ClientboundContainerSetDataPacket(int $$0, int $$1, int $$2) {
        this.containerId = $$0;
        this.id = $$1;
        this.value = $$2;
    }

    private ClientboundContainerSetDataPacket(FriendlyByteBuf $$0) {
        this.containerId = $$0.readContainerId();
        this.id = $$0.readShort();
        this.value = $$0.readShort();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeContainerId(this.containerId);
        $$0.writeShort(this.id);
        $$0.writeShort(this.value);
    }

    @Override
    public PacketType<ClientboundContainerSetDataPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_DATA;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleContainerSetData(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getId() {
        return this.id;
    }

    public int getValue() {
        return this.value;
    }
}

