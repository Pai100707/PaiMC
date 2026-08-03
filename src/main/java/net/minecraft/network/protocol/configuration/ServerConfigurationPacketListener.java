package net.minecraft.network.protocol.configuration;

import net.minecraft.network.protocol.common.ServerCommonPacketListener;

public interface ServerConfigurationPacketListener extends ServerCommonPacketListener {
   @Override
   default net.minecraft.network.ConnectionProtocol protocol() {
      return net.minecraft.network.ConnectionProtocol.CONFIGURATION;
   }

   void handleConfigurationFinished(ServerboundFinishConfigurationPacket var1);

   void handleSelectKnownPacks(ServerboundSelectKnownPacks var1);

   void handleAcceptCodeOfConduct(ServerboundAcceptCodeOfConductPacket var1);
}
