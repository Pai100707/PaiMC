package net.minecraft.network.protocol.game;

import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;

public record ClientboundTeleportEntityPacket(int id, PositionMoveRotation change, Set<Relative> relatives, boolean onGround)
   implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundTeleportEntityPacket> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      ClientboundTeleportEntityPacket::id,
      PositionMoveRotation.STREAM_CODEC,
      ClientboundTeleportEntityPacket::change,
      Relative.SET_STREAM_CODEC,
      ClientboundTeleportEntityPacket::relatives,
      ByteBufCodecs.BOOL,
      ClientboundTeleportEntityPacket::onGround,
      ClientboundTeleportEntityPacket::new
   );

   public static ClientboundTeleportEntityPacket teleport(int $$0, PositionMoveRotation $$1, Set<Relative> $$2, boolean $$3) {
      return new ClientboundTeleportEntityPacket($$0, $$1, $$2, $$3);
   }

   @Override
   public PacketType<ClientboundTeleportEntityPacket> type() {
      return GamePacketTypes.CLIENTBOUND_TELEPORT_ENTITY;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleTeleportEntity(this);
   }
}
