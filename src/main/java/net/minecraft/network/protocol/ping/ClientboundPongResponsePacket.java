package net.minecraft.network.protocol.ping;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundPongResponsePacket(long time) implements Packet<ClientPongPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundPongResponsePacket> STREAM_CODEC = Packet.codec(
      ClientboundPongResponsePacket::write, ClientboundPongResponsePacket::new
   );

   private ClientboundPongResponsePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readLong());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeLong(this.time);
   }

   @Override
   public PacketType<ClientboundPongResponsePacket> type() {
      return PingPacketTypes.CLIENTBOUND_PONG_RESPONSE;
   }

   public void handle(ClientPongPacketListener $$0) {
      $$0.handlePongResponse(this);
   }
}
