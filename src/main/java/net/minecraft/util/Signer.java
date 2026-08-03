package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.security.PrivateKey;
import java.security.Signature;
import org.slf4j.Logger;

public interface Signer {
   Logger LOGGER = LogUtils.getLogger();

   byte[] sign(net.minecraft.util.SignatureUpdater var1);

   default byte[] sign(byte[] $$0) {
      return this.sign($$1 -> $$1.update($$0));
   }

   static net.minecraft.util.Signer from(PrivateKey $$0, String $$1) {
      return $$2 -> {
         try {
            Signature $$3 = Signature.getInstance($$1);
            $$3.initSign($$0);
            $$2.update($$3::update);
            return $$3.sign();
         } catch (Exception var4) {
            throw new IllegalStateException("Failed to sign message", var4);
         }
      };
   }
}
