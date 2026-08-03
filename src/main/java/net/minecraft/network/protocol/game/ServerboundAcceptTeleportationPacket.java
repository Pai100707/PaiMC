package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundAcceptTeleportationPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundAcceptTeleportationPacket> STREAM_CODEC = Packet.codec(
      ServerboundAcceptTeleportationPacket::write, ServerboundAcceptTeleportationPacket::new
   );
   private final int id;

   public ServerboundAcceptTeleportationPacket(int $$0) {
      this.id = $$0;
   }

   private ServerboundAcceptTeleportationPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.id = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.id);
   }

   @Override
   public PacketType<ServerboundAcceptTeleportationPacket> type() {
      return GamePacketTypes.SERVERBOUND_ACCEPT_TELEPORTATION;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleAcceptTeleportPacket(this);
   }

   public int getId() {
      return this.id;
   }
}
