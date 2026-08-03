package net.minecraft.network;

import io.netty.handler.codec.EncoderException;
import net.minecraft.network.codec.IdDispatchCodec;

public class SkipPacketEncoderException extends EncoderException implements IdDispatchCodec.DontDecorateException, net.minecraft.network.SkipPacketException {
   public SkipPacketEncoderException(String $$0) {
      super($$0);
   }

   public SkipPacketEncoderException(Throwable $$0) {
      super($$0);
   }
}
