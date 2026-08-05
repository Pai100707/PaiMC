/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ClientboundEntityEventPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundEntityEventPacket> STREAM_CODEC = Packet.codec(ClientboundEntityEventPacket::write, ClientboundEntityEventPacket::new);
    private final int entityId;
    private final byte eventId;

    public ClientboundEntityEventPacket(Entity $$0, byte $$1) {
        this.entityId = $$0.getId();
        this.eventId = $$1;
    }

    private ClientboundEntityEventPacket(FriendlyByteBuf $$0) {
        this.entityId = $$0.readInt();
        this.eventId = $$0.readByte();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeInt(this.entityId);
        $$0.writeByte(this.eventId);
    }

    @Override
    public PacketType<ClientboundEntityEventPacket> type() {
        return GamePacketTypes.CLIENTBOUND_ENTITY_EVENT;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleEntityEvent(this);
    }

    public @Nullable Entity getEntity(Level $$0) {
        return $$0.getEntity(this.entityId);
    }

    public byte getEventId() {
        return this.eventId;
    }
}

