package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record DiscardedPayload(Identifier id) implements CustomPacketPayload {
   public static <T extends net.minecraft.network.FriendlyByteBuf> StreamCodec<T, DiscardedPayload> codec(Identifier $$0, int $$1) {
      return CustomPacketPayload.codec(($$0x, $$1x) -> {}, $$2 -> {
         int $$3 = $$2.readableBytes();
         if ($$3 >= 0 && $$3 <= $$1) {
            $$2.skipBytes($$3);
            return new DiscardedPayload($$0);
         } else {
            throw new IllegalArgumentException("Payload may not be larger than " + $$1 + " bytes");
         }
      });
   }

   @Override
   public CustomPacketPayload.Type<DiscardedPayload> type() {
      return new CustomPacketPayload.Type<>(this.id);
   }
}
