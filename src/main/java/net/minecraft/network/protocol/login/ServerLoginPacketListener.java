package net.minecraft.network.protocol.login;

import net.minecraft.network.protocol.cookie.ServerCookiePacketListener;

public interface ServerLoginPacketListener extends ServerCookiePacketListener {
   @Override
   default net.minecraft.network.ConnectionProtocol protocol() {
      return net.minecraft.network.ConnectionProtocol.LOGIN;
   }

   void handleHello(ServerboundHelloPacket var1);

   void handleKey(ServerboundKeyPacket var1);

   void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket var1);

   void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket var1);
}
