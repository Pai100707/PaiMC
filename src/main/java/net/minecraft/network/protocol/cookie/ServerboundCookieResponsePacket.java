package net.minecraft.network.protocol.cookie;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.resources.Identifier;

public record ServerboundCookieResponsePacket(Identifier key, byte[] payload) implements Packet<ServerCookiePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundCookieResponsePacket> STREAM_CODEC = Packet.codec(
      ServerboundCookieResponsePacket::write, ServerboundCookieResponsePacket::new
   );

   private ServerboundCookieResponsePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readIdentifier(), $$0.readNullable(ClientboundStoreCookiePacket.PAYLOAD_STREAM_CODEC));
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeIdentifier(this.key);
      $$0.writeNullable(this.payload, ClientboundStoreCookiePacket.PAYLOAD_STREAM_CODEC);
   }

   @Override
   public PacketType<ServerboundCookieResponsePacket> type() {
      return CookiePacketTypes.SERVERBOUND_COOKIE_RESPONSE;
   }

   public void handle(ServerCookiePacketListener $$0) {
      $$0.handleCookieResponse(this);
   }
}
