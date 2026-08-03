package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;

public record ClientboundEntityPositionSyncPacket(int id, PositionMoveRotation values, boolean onGround) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundEntityPositionSyncPacket> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      ClientboundEntityPositionSyncPacket::id,
      PositionMoveRotation.STREAM_CODEC,
      ClientboundEntityPositionSyncPacket::values,
      ByteBufCodecs.BOOL,
      ClientboundEntityPositionSyncPacket::onGround,
      ClientboundEntityPositionSyncPacket::new
   );

   public static ClientboundEntityPositionSyncPacket of(Entity $$0) {
      return new ClientboundEntityPositionSyncPacket(
         $$0.getId(), new PositionMoveRotation($$0.trackingPosition(), $$0.getDeltaMovement(), $$0.getYRot(), $$0.getXRot()), $$0.onGround()
      );
   }

   @Override
   public PacketType<ClientboundEntityPositionSyncPacket> type() {
      return GamePacketTypes.CLIENTBOUND_ENTITY_POSITION_SYNC;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleEntityPositionSync(this);
   }
}
