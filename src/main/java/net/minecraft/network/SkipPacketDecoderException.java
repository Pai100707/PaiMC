package net.minecraft.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.IdDispatchCodec;

public class SkipPacketDecoderException extends DecoderException implements IdDispatchCodec.DontDecorateException, net.minecraft.network.SkipPacketException {
   public SkipPacketDecoderException(String $$0) {
      super($$0);
   }

   public SkipPacketDecoderException(Throwable $$0) {
      super($$0);
   }
}
