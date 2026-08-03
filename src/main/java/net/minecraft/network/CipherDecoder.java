package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;

public class CipherDecoder extends MessageToMessageDecoder<ByteBuf> {
   private final net.minecraft.network.CipherBase cipher;

   public CipherDecoder(Cipher $$0) {
      this.cipher = new net.minecraft.network.CipherBase($$0);
   }

   protected void decode(ChannelHandlerContext $$0, ByteBuf $$1, List<Object> $$2) throws Exception {
      $$2.add(this.cipher.decipher($$0, $$1));
   }
}
