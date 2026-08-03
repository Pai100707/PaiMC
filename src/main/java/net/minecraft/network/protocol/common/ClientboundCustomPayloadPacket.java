package net.minecraft.network.protocol.common;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.util.Util;

public record ClientboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ClientCommonPacketListener> {
   private static final int MAX_PAYLOAD_SIZE = 1048576;
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundCustomPayloadPacket> GAMEPLAY_STREAM_CODEC = CustomPacketPayload.<net.minecraft.network.RegistryFriendlyByteBuf>codec(
         $$0 -> DiscardedPayload.codec($$0, 1048576),
         (List<CustomPacketPayload.TypeAndCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, ?>>)Util.make(
            Lists.newArrayList(new CustomPacketPayload.TypeAndCodec[]{new CustomPacketPayload.TypeAndCodec<>(BrandPayload.TYPE, BrandPayload.STREAM_CODEC)}),
            $$0 -> {}
         )
      )
      .map(ClientboundCustomPayloadPacket::new, ClientboundCustomPayloadPacket::payload);
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundCustomPayloadPacket> CONFIG_STREAM_CODEC = CustomPacketPayload.<net.minecraft.network.FriendlyByteBuf>codec(
         $$0 -> DiscardedPayload.codec($$0, 1048576), List.of(new CustomPacketPayload.TypeAndCodec<>(BrandPayload.TYPE, BrandPayload.STREAM_CODEC))
      )
      .map(ClientboundCustomPayloadPacket::new, ClientboundCustomPayloadPacket::payload);

   @Override
   public PacketType<ClientboundCustomPayloadPacket> type() {
      return CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD;
   }

   public void handle(ClientCommonPacketListener $$0) {
      $$0.handleCustomPayload(this);
   }
}
