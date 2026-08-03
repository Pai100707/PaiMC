package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCounted;

public record HiddenByteBuf(ByteBuf contents) implements ReferenceCounted {
   public HiddenByteBuf(final ByteBuf contents) {
      this.contents = ByteBufUtil.ensureAccessible(contents);
   }

   public static Object pack(Object $$0) {
      return $$0 instanceof ByteBuf $$1 ? new net.minecraft.network.HiddenByteBuf($$1) : $$0;
   }

   public static Object unpack(Object $$0) {
      return $$0 instanceof net.minecraft.network.HiddenByteBuf $$1 ? ByteBufUtil.ensureAccessible($$1.contents) : $$0;
   }

   public int refCnt() {
      return this.contents.refCnt();
   }

   public net.minecraft.network.HiddenByteBuf retain() {
      this.contents.retain();
      return this;
   }

   public net.minecraft.network.HiddenByteBuf retain(int $$0) {
      this.contents.retain($$0);
      return this;
   }

   public net.minecraft.network.HiddenByteBuf touch() {
      this.contents.touch();
      return this;
   }

   public net.minecraft.network.HiddenByteBuf touch(Object $$0) {
      this.contents.touch($$0);
      return this;
   }

   public boolean release() {
      return this.contents.release();
   }

   public boolean release(int $$0) {
      return this.contents.release($$0);
   }
}
