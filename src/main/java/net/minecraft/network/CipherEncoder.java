package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;

public class CipherEncoder extends MessageToByteEncoder<ByteBuf> {
   private final net.minecraft.network.CipherBase cipher;

   public CipherEncoder(Cipher $$0) {
      this.cipher = new net.minecraft.network.CipherBase($$0);
   }

   protected void encode(ChannelHandlerContext $$0, ByteBuf $$1, ByteBuf $$2) throws Exception {
      this.cipher.encipher($$1, $$2);
   }
}
