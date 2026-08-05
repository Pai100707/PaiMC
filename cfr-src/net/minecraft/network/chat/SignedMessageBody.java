/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  com.google.common.primitives.Longs
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageBody(String content, Instant timeStamp, long salt, LastSeenMessages lastSeen) {
    public static final MapCodec<SignedMessageBody> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.STRING.fieldOf("content").forGetter(SignedMessageBody::content), (App)ExtraCodecs.INSTANT_ISO8601.fieldOf("time_stamp").forGetter(SignedMessageBody::timeStamp), (App)Codec.LONG.fieldOf("salt").forGetter(SignedMessageBody::salt), (App)LastSeenMessages.CODEC.optionalFieldOf("last_seen", (Object)LastSeenMessages.EMPTY).forGetter(SignedMessageBody::lastSeen)).apply((Applicative)$$0, SignedMessageBody::new));

    public static SignedMessageBody unsigned(String $$0) {
        return new SignedMessageBody($$0, Instant.now(), 0L, LastSeenMessages.EMPTY);
    }

    public void updateSignature(SignatureUpdater.Output $$0) throws SignatureException {
        $$0.update(Longs.toByteArray((long)this.salt));
        $$0.update(Longs.toByteArray((long)this.timeStamp.getEpochSecond()));
        byte[] $$1 = this.content.getBytes(StandardCharsets.UTF_8);
        $$0.update(Ints.toByteArray((int)$$1.length));
        $$0.update($$1);
        this.lastSeen.updateSignature($$0);
    }

    public Packed pack(MessageSignatureCache $$0) {
        return new Packed(this.content, this.timeStamp, this.salt, this.lastSeen.pack($$0));
    }

    public record Packed(String content, Instant timeStamp, long salt, LastSeenMessages.Packed lastSeen) {
        public Packed(FriendlyByteBuf $$0) {
            this($$0.readUtf(256), $$0.readInstant(), $$0.readLong(), new LastSeenMessages.Packed($$0));
        }

        public void write(FriendlyByteBuf $$0) {
            $$0.writeUtf(this.content, 256);
            $$0.writeInstant(this.timeStamp);
            $$0.writeLong(this.salt);
            this.lastSeen.write($$0);
        }

        public Optional<SignedMessageBody> unpack(MessageSignatureCache $$02) {
            return this.lastSeen.unpack($$02).map($$0 -> new SignedMessageBody(this.content, this.timeStamp, this.salt, (LastSeenMessages)$$0));
        }
    }
}

