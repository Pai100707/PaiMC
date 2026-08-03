package net.minecraft.network.protocol.configuration;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;

public record ClientboundUpdateEnabledFeaturesPacket(Set<Identifier> features) implements Packet<ClientConfigurationPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundUpdateEnabledFeaturesPacket> STREAM_CODEC = Packet.codec(
      ClientboundUpdateEnabledFeaturesPacket::write, ClientboundUpdateEnabledFeaturesPacket::new
   );

   private ClientboundUpdateEnabledFeaturesPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readCollection(HashSet::new, net.minecraft.network.FriendlyByteBuf::readIdentifier));
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeCollection(this.features, net.minecraft.network.FriendlyByteBuf::writeIdentifier);
   }

   @Override
   public PacketType<ClientboundUpdateEnabledFeaturesPacket> type() {
      return ConfigurationPacketTypes.CLIENTBOUND_UPDATE_ENABLED_FEATURES;
   }

   public void handle(ClientConfigurationPacketListener $$0) {
      $$0.handleEnabledFeatures(this);
   }
}
