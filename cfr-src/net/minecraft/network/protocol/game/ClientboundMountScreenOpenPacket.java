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

public class ClientboundMountScreenOpenPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundMountScreenOpenPacket> STREAM_CODEC = Packet.codec(ClientboundMountScreenOpenPacket::write, ClientboundMountScreenOpenPacket::new);
    private final int containerId;
    private final int inventoryColumns;
    private final int entityId;

    public ClientboundMountScreenOpenPacket(int $$0, int $$1, int $$2) {
        this.containerId = $$0;
        this.inventoryColumns = $$1;
        this.entityId = $$2;
    }

    private ClientboundMountScreenOpenPacket(FriendlyByteBuf $$0) {
        this.containerId = $$0.readContainerId();
        this.inventoryColumns = $$0.readVarInt();
        this.entityId = $$0.readInt();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeContainerId(this.containerId);
        $$0.writeVarInt(this.inventoryColumns);
        $$0.writeInt(this.entityId);
    }

    @Override
    public PacketType<ClientboundMountScreenOpenPacket> type() {
        return GamePacketTypes.CLIENTBOUND_MOUNT_SCREEN_OPEN;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleMountScreenOpen(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getInventoryColumns() {
        return this.inventoryColumns;
    }

    public int getEntityId() {
        return this.entityId;
    }
}

