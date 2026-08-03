package net.minecraft.server.jsonrpc.security;

import java.security.SecureRandom;

public record SecurityConfig(String secretKey) {
   private static final String SECRET_KEY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

   public static boolean isValid(String $$0) {
      return $$0.isEmpty() ? false : $$0.matches("^[a-zA-Z0-9]{40}$");
   }

   public static String generateSecretKey() {
      SecureRandom $$0 = new SecureRandom();
      StringBuilder $$1 = new StringBuilder(40);

      for (int $$2 = 0; $$2 < 40; $$2++) {
         $$1.append(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
               .charAt($$0.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length()))
         );
      }

      return $$1.toString();
   }
}
