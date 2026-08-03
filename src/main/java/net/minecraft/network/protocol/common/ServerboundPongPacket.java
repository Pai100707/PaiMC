package net.minecraft.network.protocol.common;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundPongPacket implements Packet<ServerCommonPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundPongPacket> STREAM_CODEC = Packet.codec(
      ServerboundPongPacket::write, ServerboundPongPacket::new
   );
   private final int id;

   public ServerboundPongPacket(int $$0) {
      this.id = $$0;
   }

   private ServerboundPongPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.id = $$0.readInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeInt(this.id);
   }

   @Override
   public PacketType<ServerboundPongPacket> type() {
      return CommonPacketTypes.SERVERBOUND_PONG;
   }

   public void handle(ServerCommonPacketListener $$0) {
      $$0.handlePong(this);
   }

   public int getId() {
      return this.id;
   }
}
