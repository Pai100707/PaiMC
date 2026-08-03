package net.minecraft.network.protocol.common.custom;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.resources.Identifier;

public interface CustomPacketPayload {
   CustomPacketPayload.Type<? extends CustomPacketPayload> type();

   static <B extends ByteBuf, T extends CustomPacketPayload> StreamCodec<B, T> codec(StreamMemberEncoder<B, T> $$0, StreamDecoder<B, T> $$1) {
      return StreamCodec.ofMember($$0, $$1);
   }

   static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String $$0) {
      return new CustomPacketPayload.Type<>(Identifier.withDefaultNamespace($$0));
   }

   static <B extends net.minecraft.network.FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> codec(
      final CustomPacketPayload.FallbackProvider<B> $$0, List<CustomPacketPayload.TypeAndCodec<? super B, ?>> $$1
   ) {
      final Map<Identifier, StreamCodec<? super B, ? extends CustomPacketPayload>> $$2 = $$1.stream()
         .collect(Collectors.toUnmodifiableMap($$0x -> $$0x.type().id(), CustomPacketPayload.TypeAndCodec::codec));
      return new StreamCodec<B, CustomPacketPayload>() {
         private StreamCodec<? super B, ? extends CustomPacketPayload> findCodec(Identifier $$0x) {
            StreamCodec<? super B, ? extends CustomPacketPayload> $$1x = $$2.get($$0);
            return $$1x != null ? $$1x : $$0.create($$0);
         }

         private <T extends CustomPacketPayload> void writeCap(B $$0x, CustomPacketPayload.Type<T> $$1x, CustomPacketPayload $$2x) {
            $$0.writeIdentifier($$1x.id());
            StreamCodec<B, T> $$3 = this.findCodec($$1x.id);
            $$3.encode($$0, (T)$$2);
         }

         public void encode(B $$0x, CustomPacketPayload $$1x) {
            this.writeCap($$0, $$1x.type(), $$1x);
         }

         public CustomPacketPayload decode(B $$0x) {
            Identifier $$1x = $$0.readIdentifier();
            return (CustomPacketPayload)this.findCodec($$1x).decode($$0);
         }
      };
   }

   public interface FallbackProvider<B extends net.minecraft.network.FriendlyByteBuf> {
      StreamCodec<B, ? extends CustomPacketPayload> create(Identifier var1);
   }

   public record Type<T extends CustomPacketPayload>(Identifier id) {
   }

   public record TypeAndCodec<B extends net.minecraft.network.FriendlyByteBuf, T extends CustomPacketPayload>(
      CustomPacketPayload.Type<T> type, StreamCodec<B, T> codec
   ) {
   }
}
