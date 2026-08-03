package net.minecraft.network.protocol.status;

import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.network.protocol.ping.ServerPingPacketListener;

public interface ServerStatusPacketListener extends ServerPacketListener, ServerPingPacketListener {
   @Override
   default net.minecraft.network.ConnectionProtocol protocol() {
      return net.minecraft.network.ConnectionProtocol.STATUS;
   }

   void handleStatusRequest(ServerboundStatusRequestPacket var1);
}
