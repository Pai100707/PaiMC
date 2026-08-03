package net.minecraft.network.protocol.cookie;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;

public record ClientboundCookieRequestPacket(Identifier key) implements Packet<ClientCookiePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundCookieRequestPacket> STREAM_CODEC = Packet.codec(
      ClientboundCookieRequestPacket::write, ClientboundCookieRequestPacket::new
   );

   private ClientboundCookieRequestPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readIdentifier());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeIdentifier(this.key);
   }

   @Override
   public PacketType<ClientboundCookieRequestPacket> type() {
      return CookiePacketTypes.CLIENTBOUND_COOKIE_REQUEST;
   }

   public void handle(ClientCookiePacketListener $$0) {
      $$0.handleRequestCookie(this);
   }
}
