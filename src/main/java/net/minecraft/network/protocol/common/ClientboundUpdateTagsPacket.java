package net.minecraft.network.protocol.common;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization.NetworkPayload;

public class ClientboundUpdateTagsPacket implements Packet<ClientCommonPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundUpdateTagsPacket> STREAM_CODEC = Packet.codec(
      ClientboundUpdateTagsPacket::write, ClientboundUpdateTagsPacket::new
   );
   private final Map<ResourceKey<? extends Registry<?>>, NetworkPayload> tags;

   public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, NetworkPayload> $$0) {
      this.tags = $$0;
   }

   private ClientboundUpdateTagsPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.tags = $$0.readMap(net.minecraft.network.FriendlyByteBuf::readRegistryKey, NetworkPayload::read);
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeMap(this.tags, net.minecraft.network.FriendlyByteBuf::writeResourceKey, ($$0x, $$1) -> $$1.write($$0x));
   }

   @Override
   public PacketType<ClientboundUpdateTagsPacket> type() {
      return CommonPacketTypes.CLIENTBOUND_UPDATE_TAGS;
   }

   public void handle(ClientCommonPacketListener $$0) {
      $$0.handleUpdateTags(this);
   }

   public Map<ResourceKey<? extends Registry<?>>, NetworkPayload> getTags() {
      return this.tags;
   }
}
