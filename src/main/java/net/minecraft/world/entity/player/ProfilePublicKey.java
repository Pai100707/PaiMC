package net.minecraft.world.entity.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraft.util.Crypt;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureValidator;

public record ProfilePublicKey(ProfilePublicKey.Data data) {
   public static final Component EXPIRED_PROFILE_PUBLIC_KEY = Component.translatable("multiplayer.disconnect.expired_public_key");
   private static final Component INVALID_SIGNATURE = Component.translatable("multiplayer.disconnect.invalid_public_key_signature");
   public static final Duration EXPIRY_GRACE_PERIOD = Duration.ofHours(8L);
   public static final Codec<ProfilePublicKey> TRUSTED_CODEC = ProfilePublicKey.Data.CODEC.xmap(ProfilePublicKey::new, ProfilePublicKey::data);

   public static ProfilePublicKey createValidated(SignatureValidator $$0, UUID $$1, ProfilePublicKey.Data $$2) throws ProfilePublicKey.ValidationException {
      if (!$$2.validateSignature($$0, $$1)) {
         throw new ProfilePublicKey.ValidationException(INVALID_SIGNATURE);
      } else {
         return new ProfilePublicKey($$2);
      }
   }

   public SignatureValidator createSignatureValidator() {
      return SignatureValidator.from(this.data.key, "SHA256withRSA");
   }

   public record Data(Instant expiresAt, PublicKey key, byte[] keySignature) {
      private static final int MAX_KEY_SIGNATURE_SIZE = 4096;
      public static final Codec<ProfilePublicKey.Data> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at").forGetter(ProfilePublicKey.Data::expiresAt),
               Crypt.PUBLIC_KEY_CODEC.fieldOf("key").forGetter(ProfilePublicKey.Data::key),
               ExtraCodecs.BASE64_STRING.fieldOf("signature_v2").forGetter(ProfilePublicKey.Data::keySignature)
            )
            .apply($$0, ProfilePublicKey.Data::new)
      );

      public Data(FriendlyByteBuf $$0) {
         this($$0.readInstant(), $$0.readPublicKey(), $$0.readByteArray(4096));
      }

      public void write(FriendlyByteBuf $$0) {
         $$0.writeInstant(this.expiresAt);
         $$0.writePublicKey(this.key);
         $$0.writeByteArray(this.keySignature);
      }

      boolean validateSignature(SignatureValidator $$0, UUID $$1) {
         return $$0.validate(this.signedPayload($$1), this.keySignature);
      }

      private byte[] signedPayload(UUID $$0) {
         byte[] $$1 = this.key.getEncoded();
         byte[] $$2 = new byte[24 + $$1.length];
         ByteBuffer $$3 = ByteBuffer.wrap($$2).order(ByteOrder.BIG_ENDIAN);
         $$3.putLong($$0.getMostSignificantBits()).putLong($$0.getLeastSignificantBits()).putLong(this.expiresAt.toEpochMilli()).put($$1);
         return $$2;
      }

      public boolean hasExpired() {
         return this.expiresAt.isBefore(Instant.now());
      }

      public boolean hasExpired(Duration $$0) {
         return this.expiresAt.plus($$0).isBefore(Instant.now());
      }

      @Override
      public boolean equals(Object $$0) {
         return !($$0 instanceof ProfilePublicKey.Data $$1)
            ? false
            : this.expiresAt.equals($$1.expiresAt) && this.key.equals($$1.key) && Arrays.equals(this.keySignature, $$1.keySignature);
      }
   }

   public static class ValidationException extends ThrowingComponent {
      public ValidationException(Component $$0) {
         super($$0);
      }
   }
}
