package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;

public class Varint21FrameDecoder extends ByteToMessageDecoder {
   private static final int MAX_VARINT21_BYTES = 3;
   private final ByteBuf helperBuf = Unpooled.directBuffer(3);
   
   private final net.minecraft.network.BandwidthDebugMonitor monitor;

   public Varint21FrameDecoder(net.minecraft.network.BandwidthDebugMonitor $$0) {
      this.monitor = $$0;
   }

   protected void handlerRemoved0(ChannelHandlerContext $$0) {
      this.helperBuf.release();
   }

   private static boolean copyVarint(ByteBuf $$0, ByteBuf $$1) {
      for (int $$2 = 0; $$2 < 3; $$2++) {
         if (!$$0.isReadable()) {
            return false;
         }

         byte $$3 = $$0.readByte();
         $$1.writeByte($$3);
         if (!net.minecraft.network.VarInt.hasContinuationBit($$3)) {
            return true;
         }
      }

      throw new CorruptedFrameException("length wider than 21-bit");
   }

   protected void decode(ChannelHandlerContext $$0, ByteBuf $$1, List<Object> $$2) {
      $$1.markReaderIndex();
      this.helperBuf.clear();
      if (!copyVarint($$1, this.helperBuf)) {
         $$1.resetReaderIndex();
      } else {
         int $$3 = net.minecraft.network.VarInt.read(this.helperBuf);
         if ($$3 == 0) {
            throw new CorruptedFrameException("Frame length cannot be zero");
         } else if ($$1.readableBytes() < $$3) {
            $$1.resetReaderIndex();
         } else {
            if (this.monitor != null) {
               this.monitor.onReceive($$3 + net.minecraft.network.VarInt.getByteSize($$3));
            }

            $$2.add($$1.readBytes($$3));
         }
      }
   }
}
