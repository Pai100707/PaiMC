package net.minecraft.network.protocol.common;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundPingPacket implements Packet<ClientCommonPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundPingPacket> STREAM_CODEC = Packet.codec(
      ClientboundPingPacket::write, ClientboundPingPacket::new
   );
   private final int id;

   public ClientboundPingPacket(int $$0) {
      this.id = $$0;
   }

   private ClientboundPingPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.id = $$0.readInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeInt(this.id);
   }

   @Override
   public PacketType<ClientboundPingPacket> type() {
      return CommonPacketTypes.CLIENTBOUND_PING;
   }

   public void handle(ClientCommonPacketListener $$0) {
      $$0.handlePing(this);
   }

   public int getId() {
      return this.id;
   }
}
