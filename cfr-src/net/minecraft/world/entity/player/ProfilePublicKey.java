/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.player;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
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

public record ProfilePublicKey(Data data) {
    public static final Component EXPIRED_PROFILE_PUBLIC_KEY = Component.translatable("multiplayer.disconnect.expired_public_key");
    private static final Component INVALID_SIGNATURE = Component.translatable("multiplayer.disconnect.invalid_public_key_signature");
    public static final Duration EXPIRY_GRACE_PERIOD = Duration.ofHours(8L);
    public static final Codec<ProfilePublicKey> TRUSTED_CODEC = Data.CODEC.xmap(ProfilePublicKey::new, ProfilePublicKey::data);

    public static ProfilePublicKey createValidated(SignatureValidator $$0, UUID $$1, Data $$2) throws ValidationException {
        if (!$$2.validateSignature($$0, $$1)) {
            throw new ValidationException(INVALID_SIGNATURE);
        }
        return new ProfilePublicKey($$2);
    }

    public SignatureValidator createSignatureValidator() {
        return SignatureValidator.from(this.data.key, "SHA256withRSA");
    }

    public static final class Data
    extends Record {
        private final Instant expiresAt;
        final PublicKey key;
        private final byte[] keySignature;
        private static final int MAX_KEY_SIGNATURE_SIZE = 4096;
        public static final Codec<Data> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at").forGetter(Data::expiresAt), (App)Crypt.PUBLIC_KEY_CODEC.fieldOf("key").forGetter(Data::key), (App)ExtraCodecs.BASE64_STRING.fieldOf("signature_v2").forGetter(Data::keySignature)).apply((Applicative)$$0, Data::new));

        public Data(FriendlyByteBuf $$0) {
            this($$0.readInstant(), $$0.readPublicKey(), $$0.readByteArray(4096));
        }

        public Data(Instant $$0, PublicKey $$1, byte[] $$2) {
            this.expiresAt = $$0;
            this.key = $$1;
            this.keySignature = $$2;
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
            if ($$0 instanceof Data) {
                Data $$1 = (Data)$$0;
                return this.expiresAt.equals($$1.expiresAt) && this.key.equals($$1.key) && Arrays.equals(this.keySignature, $$1.keySignature);
            }
            return false;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Data.class, "expiresAt;key;keySignature", "expiresAt", "key", "keySignature"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Data.class, "expiresAt;key;keySignature", "expiresAt", "key", "keySignature"}, this);
        }

        public Instant expiresAt() {
            return this.expiresAt;
        }

        public PublicKey key() {
            return this.key;
        }

        public byte[] keySignature() {
            return this.keySignature;
        }
    }

    public static class ValidationException
    extends ThrowingComponent {
        public ValidationException(Component $$0) {
            super($$0);
        }
    }
}

