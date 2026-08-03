package net.minecraft.network.protocol.ping;

public interface ClientPongPacketListener extends net.minecraft.network.PacketListener {
   void handlePongResponse(ClientboundPongResponsePacket var1);
}
