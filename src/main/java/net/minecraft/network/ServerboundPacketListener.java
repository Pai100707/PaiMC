package net.minecraft.network;

import net.minecraft.network.protocol.PacketFlow;

public interface ServerboundPacketListener extends net.minecraft.network.PacketListener {
   @Override
   default PacketFlow flow() {
      return PacketFlow.SERVERBOUND;
   }
}
