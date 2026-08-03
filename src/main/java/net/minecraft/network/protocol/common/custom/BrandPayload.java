package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.codec.StreamCodec;

public record BrandPayload(String brand) implements CustomPacketPayload {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, BrandPayload> STREAM_CODEC = CustomPacketPayload.codec(
      BrandPayload::write, BrandPayload::new
   );
   public static final CustomPacketPayload.Type<BrandPayload> TYPE = CustomPacketPayload.createType("brand");

   private BrandPayload(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readUtf());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeUtf(this.brand);
   }

   @Override
   public CustomPacketPayload.Type<BrandPayload> type() {
      return TYPE;
   }
}
