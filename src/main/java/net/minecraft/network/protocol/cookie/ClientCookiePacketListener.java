package net.minecraft.network.protocol.cookie;

public interface ClientCookiePacketListener extends net.minecraft.network.ClientboundPacketListener {
   void handleRequestCookie(ClientboundCookieRequestPacket var1);
}
