/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record ServerboundMoveVehiclePacket(Vec3 position, float yRot, float xRot, boolean onGround) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundMoveVehiclePacket> STREAM_CODEC = StreamCodec.composite(Vec3.STREAM_CODEC, ServerboundMoveVehiclePacket::position, ByteBufCodecs.FLOAT, ServerboundMoveVehiclePacket::yRot, ByteBufCodecs.FLOAT, ServerboundMoveVehiclePacket::xRot, ByteBufCodecs.BOOL, ServerboundMoveVehiclePacket::onGround, ServerboundMoveVehiclePacket::new);

    public static ServerboundMoveVehiclePacket fromEntity(Entity $$0) {
        if ($$0.isInterpolating()) {
            return new ServerboundMoveVehiclePacket($$0.getInterpolation().position(), $$0.getInterpolation().yRot(), $$0.getInterpolation().xRot(), $$0.onGround());
        }
        return new ServerboundMoveVehiclePacket($$0.position(), $$0.getYRot(), $$0.getXRot(), $$0.onGround());
    }

    @Override
    public PacketType<ServerboundMoveVehiclePacket> type() {
        return GamePacketTypes.SERVERBOUND_MOVE_VEHICLE;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleMoveVehicle(this);
    }
}

