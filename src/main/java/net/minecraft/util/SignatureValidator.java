package net.minecraft.util;

import com.mojang.authlib.yggdrasil.ServicesKeyInfo;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.logging.LogUtils;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collection;
import org.slf4j.Logger;

public interface SignatureValidator {
   net.minecraft.util.SignatureValidator NO_VALIDATION = ($$0, $$1) -> true;
   Logger LOGGER = LogUtils.getLogger();

   boolean validate(net.minecraft.util.SignatureUpdater var1, byte[] var2);

   default boolean validate(byte[] $$0, byte[] $$1) {
      return this.validate($$1x -> $$1x.update($$0), $$1);
   }

   private static boolean verifySignature(net.minecraft.util.SignatureUpdater $$0, byte[] $$1, Signature $$2) throws SignatureException {
      $$0.update($$2::update);
      return $$2.verify($$1);
   }

   static net.minecraft.util.SignatureValidator from(PublicKey $$0, String $$1) {
      return ($$2, $$3) -> {
         try {
            Signature $$4 = Signature.getInstance($$1);
            $$4.initVerify($$0);
            return verifySignature($$2, $$3, $$4);
         } catch (Exception var5) {
            LOGGER.error("Failed to verify signature", var5);
            return false;
         }
      };
   }

   
   static net.minecraft.util.SignatureValidator from(ServicesKeySet $$0, ServicesKeyType $$1) {
      Collection<ServicesKeyInfo> $$2 = $$0.keys($$1);
      return $$2.isEmpty() ? null : ($$1x, $$2x) -> $$2.stream().anyMatch($$2xx -> {
         Signature $$3 = $$2xx.signature();

         try {
            return verifySignature($$1x, $$2x, $$3);
         } catch (SignatureException var5) {
            LOGGER.error("Failed to verify Services signature", var5);
            return false;
         }
      });
   }
}
