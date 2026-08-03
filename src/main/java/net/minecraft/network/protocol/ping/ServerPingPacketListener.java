package net.minecraft.network.protocol.ping;

public interface ServerPingPacketListener extends net.minecraft.network.PacketListener {
   void handlePingRequest(ServerboundPingRequestPacket var1);
}
