package net.minecraft.util;

import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Encoder;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.network.FriendlyByteBuf;

public class Crypt {
   private static final String SYMMETRIC_ALGORITHM = "AES";
   private static final int SYMMETRIC_BITS = 128;
   private static final String ASYMMETRIC_ALGORITHM = "RSA";
   private static final int ASYMMETRIC_BITS = 1024;
   private static final String BYTE_ENCODING = "ISO_8859_1";
   private static final String HASH_ALGORITHM = "SHA-1";
   public static final String SIGNING_ALGORITHM = "SHA256withRSA";
   public static final int SIGNATURE_BYTES = 256;
   private static final String PEM_RSA_PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
   private static final String PEM_RSA_PRIVATE_KEY_FOOTER = "-----END RSA PRIVATE KEY-----";
   public static final String RSA_PUBLIC_KEY_HEADER = "-----BEGIN RSA PUBLIC KEY-----";
   private static final String RSA_PUBLIC_KEY_FOOTER = "-----END RSA PUBLIC KEY-----";
   public static final String MIME_LINE_SEPARATOR = "\n";
   public static final Encoder MIME_ENCODER = Base64.getMimeEncoder(76, "\n".getBytes(StandardCharsets.UTF_8));
   public static final Codec<PublicKey> PUBLIC_KEY_CODEC = Codec.STRING.comapFlatMap($$0 -> {
      try {
         return DataResult.success(stringToRsaPublicKey($$0));
      } catch (net.minecraft.util.CryptException var2) {
         return DataResult.error(var2::getMessage);
      }
   }, net.minecraft.util.Crypt::rsaPublicKeyToString);
   public static final Codec<PrivateKey> PRIVATE_KEY_CODEC = Codec.STRING.comapFlatMap($$0 -> {
      try {
         return DataResult.success(stringToPemRsaPrivateKey($$0));
      } catch (net.minecraft.util.CryptException var2) {
         return DataResult.error(var2::getMessage);
      }
   }, net.minecraft.util.Crypt::pemRsaPrivateKeyToString);

   public static SecretKey generateSecretKey() throws net.minecraft.util.CryptException {
      try {
         KeyGenerator $$0 = KeyGenerator.getInstance("AES");
         $$0.init(128);
         return $$0.generateKey();
      } catch (Exception var1) {
         throw new net.minecraft.util.CryptException(var1);
      }
   }

   public static KeyPair generateKeyPair() throws net.minecraft.util.CryptException {
      try {
         KeyPairGenerator $$0 = KeyPairGenerator.getInstance("RSA");
         $$0.initialize(1024);
         return $$0.generateKeyPair();
      } catch (Exception var1) {
         throw new net.minecraft.util.CryptException(var1);
      }
   }

   public static byte[] digestData(String $$0, PublicKey $$1, SecretKey $$2) throws net.minecraft.util.CryptException {
      try {
         return digestData($$0.getBytes("ISO_8859_1"), $$2.getEncoded(), $$1.getEncoded());
      } catch (Exception var4) {
         throw new net.minecraft.util.CryptException(var4);
      }
   }

   private static byte[] digestData(byte[]... $$0) throws Exception {
      MessageDigest $$1 = MessageDigest.getInstance("SHA-1");

      for (byte[] $$2 : $$0) {
         $$1.update($$2);
      }

      return $$1.digest();
   }

   private static <T extends Key> T rsaStringToKey(String $$0, String $$1, String $$2, net.minecraft.util.Crypt.ByteArrayToKeyFunction<T> $$3) throws net.minecraft.util.CryptException {
      int $$4 = $$0.indexOf($$1);
      if ($$4 != -1) {
         $$4 += $$1.length();
         int $$5 = $$0.indexOf($$2, $$4);
         $$0 = $$0.substring($$4, $$5 + 1);
      }

      try {
         return $$3.apply(Base64.getMimeDecoder().decode($$0));
      } catch (IllegalArgumentException var6) {
         throw new net.minecraft.util.CryptException(var6);
      }
   }

   public static PrivateKey stringToPemRsaPrivateKey(String $$0) throws net.minecraft.util.CryptException {
      return rsaStringToKey($$0, "-----BEGIN RSA PRIVATE KEY-----", "-----END RSA PRIVATE KEY-----", net.minecraft.util.Crypt::byteToPrivateKey);
   }

   public static PublicKey stringToRsaPublicKey(String $$0) throws net.minecraft.util.CryptException {
      return rsaStringToKey($$0, "-----BEGIN RSA PUBLIC KEY-----", "-----END RSA PUBLIC KEY-----", net.minecraft.util.Crypt::byteToPublicKey);
   }

   public static String rsaPublicKeyToString(PublicKey $$0) {
      if (!"RSA".equals($$0.getAlgorithm())) {
         throw new IllegalArgumentException("Public key must be RSA");
      } else {
         return "-----BEGIN RSA PUBLIC KEY-----\n" + MIME_ENCODER.encodeToString($$0.getEncoded()) + "\n-----END RSA PUBLIC KEY-----\n";
      }
   }

   public static String pemRsaPrivateKeyToString(PrivateKey $$0) {
      if (!"RSA".equals($$0.getAlgorithm())) {
         throw new IllegalArgumentException("Private key must be RSA");
      } else {
         return "-----BEGIN RSA PRIVATE KEY-----\n" + MIME_ENCODER.encodeToString($$0.getEncoded()) + "\n-----END RSA PRIVATE KEY-----\n";
      }
   }

   private static PrivateKey byteToPrivateKey(byte[] $$0) throws net.minecraft.util.CryptException {
      try {
         EncodedKeySpec $$1 = new PKCS8EncodedKeySpec($$0);
         KeyFactory $$2 = KeyFactory.getInstance("RSA");
         return $$2.generatePrivate($$1);
      } catch (Exception var3) {
         throw new net.minecraft.util.CryptException(var3);
      }
   }

   public static PublicKey byteToPublicKey(byte[] $$0) throws net.minecraft.util.CryptException {
      try {
         EncodedKeySpec $$1 = new X509EncodedKeySpec($$0);
         KeyFactory $$2 = KeyFactory.getInstance("RSA");
         return $$2.generatePublic($$1);
      } catch (Exception var3) {
         throw new net.minecraft.util.CryptException(var3);
      }
   }

   public static SecretKey decryptByteToSecretKey(PrivateKey $$0, byte[] $$1) throws net.minecraft.util.CryptException {
      byte[] $$2 = decryptUsingKey($$0, $$1);

      try {
         return new SecretKeySpec($$2, "AES");
      } catch (Exception var4) {
         throw new net.minecraft.util.CryptException(var4);
      }
   }

   public static byte[] encryptUsingKey(Key $$0, byte[] $$1) throws net.minecraft.util.CryptException {
      return cipherData(1, $$0, $$1);
   }

   public static byte[] decryptUsingKey(Key $$0, byte[] $$1) throws net.minecraft.util.CryptException {
      return cipherData(2, $$0, $$1);
   }

   private static byte[] cipherData(int $$0, Key $$1, byte[] $$2) throws net.minecraft.util.CryptException {
      try {
         return setupCipher($$0, $$1.getAlgorithm(), $$1).doFinal($$2);
      } catch (Exception var4) {
         throw new net.minecraft.util.CryptException(var4);
      }
   }

   private static Cipher setupCipher(int $$0, String $$1, Key $$2) throws Exception {
      Cipher $$3 = Cipher.getInstance($$1);
      $$3.init($$0, $$2);
      return $$3;
   }

   public static Cipher getCipher(int $$0, Key $$1) throws net.minecraft.util.CryptException {
      try {
         Cipher $$2 = Cipher.getInstance("AES/CFB8/NoPadding");
         $$2.init($$0, $$1, new IvParameterSpec($$1.getEncoded()));
         return $$2;
      } catch (Exception var3) {
         throw new net.minecraft.util.CryptException(var3);
      }
   }

   interface ByteArrayToKeyFunction<T extends Key> {
      T apply(byte[] var1) throws net.minecraft.util.CryptException;
   }

   public record SaltSignaturePair(long salt, byte[] signature) {
      public static final net.minecraft.util.Crypt.SaltSignaturePair EMPTY = new net.minecraft.util.Crypt.SaltSignaturePair(0L, ByteArrays.EMPTY_ARRAY);

      public SaltSignaturePair(FriendlyByteBuf $$0) {
         this($$0.readLong(), $$0.readByteArray());
      }

      public boolean isValid() {
         return this.signature.length > 0;
      }

      public static void write(FriendlyByteBuf $$0, net.minecraft.util.Crypt.SaltSignaturePair $$1) {
         $$0.writeLong($$1.salt);
         $$0.writeByteArray($$1.signature);
      }

      public byte[] saltAsBytes() {
         return Longs.toByteArray(this.salt);
      }
   }

   public static class SaltSupplier {
      private static final SecureRandom secureRandom = new SecureRandom();

      public static long getLong() {
         return secureRandom.nextLong();
      }
   }
}
