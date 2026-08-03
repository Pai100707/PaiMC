package net.minecraft.network.protocol.handshake;

import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerHandshakePacketListener extends ServerPacketListener {
   @Override
   default net.minecraft.network.ConnectionProtocol protocol() {
      return net.minecraft.network.ConnectionProtocol.HANDSHAKING;
   }

   void handleIntention(ClientIntentionPacket var1);
}
