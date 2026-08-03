package net.minecraft.util;

import java.security.SignatureException;

@FunctionalInterface
public interface SignatureUpdater {
   void update(net.minecraft.util.SignatureUpdater.Output var1) throws SignatureException;

   @FunctionalInterface
   public interface Output {
      void update(byte[] var1) throws SignatureException;
   }
}
