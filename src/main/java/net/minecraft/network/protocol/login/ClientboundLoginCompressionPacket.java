package net.minecraft.network.protocol.login;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundLoginCompressionPacket implements Packet<ClientLoginPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundLoginCompressionPacket> STREAM_CODEC = Packet.codec(
      ClientboundLoginCompressionPacket::write, ClientboundLoginCompressionPacket::new
   );
   private final int compressionThreshold;

   public ClientboundLoginCompressionPacket(int $$0) {
      this.compressionThreshold = $$0;
   }

   private ClientboundLoginCompressionPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.compressionThreshold = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.compressionThreshold);
   }

   @Override
   public PacketType<ClientboundLoginCompressionPacket> type() {
      return LoginPacketTypes.CLIENTBOUND_LOGIN_COMPRESSION;
   }

   public void handle(ClientLoginPacketListener $$0) {
      $$0.handleCompression(this);
   }

   public int getCompressionThreshold() {
      return this.compressionThreshold;
   }
}
