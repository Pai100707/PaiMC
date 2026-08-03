package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;

public interface Packet<T extends net.minecraft.network.PacketListener> {
   PacketType<? extends Packet<T>> type();

   void handle(T var1);

   default boolean isSkippable() {
      return false;
   }

   default boolean isTerminal() {
      return false;
   }

   static <B extends ByteBuf, T extends Packet<?>> StreamCodec<B, T> codec(StreamMemberEncoder<B, T> $$0, StreamDecoder<B, T> $$1) {
      return StreamCodec.ofMember($$0, $$1);
   }
}
