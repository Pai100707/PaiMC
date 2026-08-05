/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class ServerboundTeleportToEntityPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundTeleportToEntityPacket> STREAM_CODEC = Packet.codec(ServerboundTeleportToEntityPacket::write, ServerboundTeleportToEntityPacket::new);
    private final UUID uuid;

    public ServerboundTeleportToEntityPacket(UUID $$0) {
        this.uuid = $$0;
    }

    private ServerboundTeleportToEntityPacket(FriendlyByteBuf $$0) {
        this.uuid = $$0.readUUID();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeUUID(this.uuid);
    }

    @Override
    public PacketType<ServerboundTeleportToEntityPacket> type() {
        return GamePacketTypes.SERVERBOUND_TELEPORT_TO_ENTITY;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleTeleportToEntityPacket(this);
    }

    public @Nullable Entity getEntity(ServerLevel $$0) {
        return $$0.getEntity(this.uuid);
    }
}

