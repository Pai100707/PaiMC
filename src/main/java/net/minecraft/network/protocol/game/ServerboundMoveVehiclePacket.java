package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record ServerboundMoveVehiclePacket(Vec3 position, float yRot, float xRot, boolean onGround) implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundMoveVehiclePacket> STREAM_CODEC = StreamCodec.composite(
      Vec3.STREAM_CODEC,
      ServerboundMoveVehiclePacket::position,
      ByteBufCodecs.FLOAT,
      ServerboundMoveVehiclePacket::yRot,
      ByteBufCodecs.FLOAT,
      ServerboundMoveVehiclePacket::xRot,
      ByteBufCodecs.BOOL,
      ServerboundMoveVehiclePacket::onGround,
      ServerboundMoveVehiclePacket::new
   );

   public static ServerboundMoveVehiclePacket fromEntity(Entity $$0) {
      return $$0.isInterpolating()
         ? new ServerboundMoveVehiclePacket($$0.getInterpolation().position(), $$0.getInterpolation().yRot(), $$0.getInterpolation().xRot(), $$0.onGround())
         : new ServerboundMoveVehiclePacket($$0.position(), $$0.getYRot(), $$0.getXRot(), $$0.onGround());
   }

   @Override
   public PacketType<ServerboundMoveVehiclePacket> type() {
      return GamePacketTypes.SERVERBOUND_MOVE_VEHICLE;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleMoveVehicle(this);
   }
}
