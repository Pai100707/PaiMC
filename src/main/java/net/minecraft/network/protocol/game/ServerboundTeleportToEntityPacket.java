package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class ServerboundTeleportToEntityPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundTeleportToEntityPacket> STREAM_CODEC = Packet.codec(
      ServerboundTeleportToEntityPacket::write, ServerboundTeleportToEntityPacket::new
   );
   private final UUID uuid;

   public ServerboundTeleportToEntityPacket(UUID $$0) {
      this.uuid = $$0;
   }

   private ServerboundTeleportToEntityPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.uuid = $$0.readUUID();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeUUID(this.uuid);
   }

   @Override
   public PacketType<ServerboundTeleportToEntityPacket> type() {
      return GamePacketTypes.SERVERBOUND_TELEPORT_TO_ENTITY;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleTeleportToEntityPacket(this);
   }

   
   public Entity getEntity(ServerLevel $$0) {
      return $$0.getEntity(this.uuid);
   }
}
