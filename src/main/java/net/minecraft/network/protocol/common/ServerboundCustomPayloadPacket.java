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

public record ServerboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ServerCommonPacketListener> {
   private static final int MAX_PAYLOAD_SIZE = 32767;
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundCustomPayloadPacket> STREAM_CODEC = CustomPacketPayload.<net.minecraft.network.FriendlyByteBuf>codec(
         $$0 -> DiscardedPayload.codec($$0, 32767),
         (List<CustomPacketPayload.TypeAndCodec<? super net.minecraft.network.FriendlyByteBuf, ?>>)Util.make(
            Lists.newArrayList(new CustomPacketPayload.TypeAndCodec[]{new CustomPacketPayload.TypeAndCodec<>(BrandPayload.TYPE, BrandPayload.STREAM_CODEC)}),
            $$0 -> {}
         )
      )
      .map(ServerboundCustomPayloadPacket::new, ServerboundCustomPayloadPacket::payload);

   @Override
   public PacketType<ServerboundCustomPayloadPacket> type() {
      return CommonPacketTypes.SERVERBOUND_CUSTOM_PAYLOAD;
   }

   public void handle(ServerCommonPacketListener $$0) {
      $$0.handleCustomPayload(this);
   }
}
