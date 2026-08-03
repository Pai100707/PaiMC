package net.minecraft.network.protocol.status;

import net.minecraft.network.protocol.ping.ClientPongPacketListener;

public interface ClientStatusPacketListener extends ClientPongPacketListener, net.minecraft.network.ClientboundPacketListener {
   @Override
   default net.minecraft.network.ConnectionProtocol protocol() {
      return net.minecraft.network.ConnectionProtocol.STATUS;
   }

   void handleStatusResponse(ClientboundStatusResponsePacket var1);
}
