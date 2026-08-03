package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class CompressionDecoder extends ByteToMessageDecoder {
   public static final int MAXIMUM_COMPRESSED_LENGTH = 2097152;
   public static final int MAXIMUM_UNCOMPRESSED_LENGTH = 8388608;
   private final Inflater inflater;
   private int threshold;
   private boolean validateDecompressed;

   public CompressionDecoder(int $$0, boolean $$1) {
      this.threshold = $$0;
      this.validateDecompressed = $$1;
      this.inflater = new Inflater();
   }

   protected void decode(ChannelHandlerContext $$0, ByteBuf $$1, List<Object> $$2) throws Exception {
      int $$3 = net.minecraft.network.VarInt.read($$1);
      if ($$3 == 0) {
         $$2.add($$1.readBytes($$1.readableBytes()));
      } else {
         if (this.validateDecompressed) {
            if ($$3 < this.threshold) {
               throw new DecoderException("Badly compressed packet - size of " + $$3 + " is below server threshold of " + this.threshold);
            }

            if ($$3 > 8388608) {
               throw new DecoderException("Badly compressed packet - size of " + $$3 + " is larger than protocol maximum of 8388608");
            }
         }

         this.setupInflaterInput($$1);
         ByteBuf $$4 = this.inflate($$0, $$3);
         this.inflater.reset();
         $$2.add($$4);
      }
   }

   private void setupInflaterInput(ByteBuf $$0) {
      ByteBuffer $$1;
      if ($$0.nioBufferCount() > 0) {
         $$1 = $$0.nioBuffer();
         $$0.skipBytes($$0.readableBytes());
      } else {
         $$1 = ByteBuffer.allocateDirect($$0.readableBytes());
         $$0.readBytes($$1);
         $$1.flip();
      }

      this.inflater.setInput($$1);
   }

   private ByteBuf inflate(ChannelHandlerContext $$0, int $$1) throws DataFormatException {
      ByteBuf $$2 = $$0.alloc().directBuffer($$1);

      try {
         ByteBuffer $$3 = $$2.internalNioBuffer(0, $$1);
         int $$4 = $$3.position();
         this.inflater.inflate($$3);
         int $$5 = $$3.position() - $$4;
         if ($$5 != $$1) {
            throw new DecoderException("Badly compressed packet - actual length of uncompressed payload " + $$5 + " is does not match declared size " + $$1);
         } else {
            $$2.writerIndex($$2.writerIndex() + $$5);
            return $$2;
         }
      } catch (Exception var7) {
         $$2.release();
         throw var7;
      }
   }

   public void setThreshold(int $$0, boolean $$1) {
      this.threshold = $$0;
      this.validateDecompressed = $$1;
   }
}
