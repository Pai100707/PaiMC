package net.minecraft.network;

public interface TickablePacketListener extends net.minecraft.network.PacketListener {
   void tick();
}
