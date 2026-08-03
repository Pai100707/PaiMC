package net.minecraft.network.protocol.common;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundKeepAlivePacket implements Packet<ClientCommonPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundKeepAlivePacket> STREAM_CODEC = Packet.codec(
      ClientboundKeepAlivePacket::write, ClientboundKeepAlivePacket::new
   );
   private final long id;

   public ClientboundKeepAlivePacket(long $$0) {
      this.id = $$0;
   }

   private ClientboundKeepAlivePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.id = $$0.readLong();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeLong(this.id);
   }

   @Override
   public PacketType<ClientboundKeepAlivePacket> type() {
      return CommonPacketTypes.CLIENTBOUND_KEEP_ALIVE;
   }

   public void handle(ClientCommonPacketListener $$0) {
      $$0.handleKeepAlive(this);
   }

   public long getId() {
      return this.id;
   }
}
