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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ClientboundRotateHeadPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundRotateHeadPacket> STREAM_CODEC = Packet.codec(ClientboundRotateHeadPacket::write, ClientboundRotateHeadPacket::new);
    private final int entityId;
    private final byte yHeadRot;

    public ClientboundRotateHeadPacket(Entity $$0, byte $$1) {
        this.entityId = $$0.getId();
        this.yHeadRot = $$1;
    }

    private ClientboundRotateHeadPacket(FriendlyByteBuf $$0) {
        this.entityId = $$0.readVarInt();
        this.yHeadRot = $$0.readByte();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.entityId);
        $$0.writeByte(this.yHeadRot);
    }

    @Override
    public PacketType<ClientboundRotateHeadPacket> type() {
        return GamePacketTypes.CLIENTBOUND_ROTATE_HEAD;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleRotateMob(this);
    }

    public @Nullable Entity getEntity(Level $$0) {
        return $$0.getEntity(this.entityId);
    }

    public float getYHeadRot() {
        return Mth.unpackDegrees(this.yHeadRot);
    }
}

