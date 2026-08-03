package net.minecraft.network.protocol.common;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundKeepAlivePacket implements Packet<ServerCommonPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundKeepAlivePacket> STREAM_CODEC = Packet.codec(
      ServerboundKeepAlivePacket::write, ServerboundKeepAlivePacket::new
   );
   private final long id;

   public ServerboundKeepAlivePacket(long $$0) {
      this.id = $$0;
   }

   private ServerboundKeepAlivePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.id = $$0.readLong();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeLong(this.id);
   }

   @Override
   public PacketType<ServerboundKeepAlivePacket> type() {
      return CommonPacketTypes.SERVERBOUND_KEEP_ALIVE;
   }

   public void handle(ServerCommonPacketListener $$0) {
      $$0.handleKeepAlive(this);
   }

   public long getId() {
      return this.id;
   }
}
