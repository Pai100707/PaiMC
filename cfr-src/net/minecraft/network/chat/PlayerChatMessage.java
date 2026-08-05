/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public record PlayerChatMessage(SignedMessageLink link, @Nullable MessageSignature signature, SignedMessageBody signedBody, @Nullable Component unsignedContent, FilterMask filterMask) {
    public static final MapCodec<PlayerChatMessage> MAP_CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)SignedMessageLink.CODEC.fieldOf("link").forGetter(PlayerChatMessage::link), (App)MessageSignature.CODEC.optionalFieldOf("signature").forGetter($$0 -> Optional.ofNullable($$0.signature)), (App)SignedMessageBody.MAP_CODEC.forGetter(PlayerChatMessage::signedBody), (App)ComponentSerialization.CODEC.optionalFieldOf("unsigned_content").forGetter($$0 -> Optional.ofNullable($$0.unsignedContent)), (App)FilterMask.CODEC.optionalFieldOf("filter_mask", (Object)FilterMask.PASS_THROUGH).forGetter(PlayerChatMessage::filterMask)).apply((Applicative)$$02, ($$0, $$1, $$2, $$3, $$4) -> new PlayerChatMessage((SignedMessageLink)$$0, $$1.orElse(null), (SignedMessageBody)$$2, $$3.orElse(null), (FilterMask)$$4)));
    private static final UUID SYSTEM_SENDER = Util.NIL_UUID;
    public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
    public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

    public static PlayerChatMessage system(String $$0) {
        return PlayerChatMessage.unsigned(SYSTEM_SENDER, $$0);
    }

    public static PlayerChatMessage unsigned(UUID $$0, String $$1) {
        SignedMessageBody $$2 = SignedMessageBody.unsigned($$1);
        SignedMessageLink $$3 = SignedMessageLink.unsigned($$0);
        return new PlayerChatMessage($$3, null, $$2, null, FilterMask.PASS_THROUGH);
    }

    public PlayerChatMessage withUnsignedContent(Component $$0) {
        Component $$1 = !$$0.equals(Component.literal(this.signedContent())) ? $$0 : null;
        return new PlayerChatMessage(this.link, this.signature, this.signedBody, $$1, this.filterMask);
    }

    public PlayerChatMessage removeUnsignedContent() {
        if (this.unsignedContent != null) {
            return new PlayerChatMessage(this.link, this.signature, this.signedBody, null, this.filterMask);
        }
        return this;
    }

    public PlayerChatMessage filter(FilterMask $$0) {
        if (this.filterMask.equals($$0)) {
            return this;
        }
        return new PlayerChatMessage(this.link, this.signature, this.signedBody, this.unsignedContent, $$0);
    }

    public PlayerChatMessage filter(boolean $$0) {
        return this.filter($$0 ? this.filterMask : FilterMask.PASS_THROUGH);
    }

    public PlayerChatMessage removeSignature() {
        SignedMessageBody $$0 = SignedMessageBody.unsigned(this.signedContent());
        SignedMessageLink $$1 = SignedMessageLink.unsigned(this.sender());
        return new PlayerChatMessage($$1, null, $$0, this.unsignedContent, this.filterMask);
    }

    public static void updateSignature(SignatureUpdater.Output $$0, SignedMessageLink $$1, SignedMessageBody $$2) throws SignatureException {
        $$0.update(Ints.toByteArray((int)1));
        $$1.updateSignature($$0);
        $$2.updateSignature($$0);
    }

    public boolean verify(SignatureValidator $$02) {
        return this.signature != null && this.signature.verify($$02, $$0 -> PlayerChatMessage.updateSignature($$0, this.link, this.signedBody));
    }

    public String signedContent() {
        return this.signedBody.content();
    }

    public Component decoratedContent() {
        return Objects.requireNonNullElseGet(this.unsignedContent, () -> Component.literal(this.signedContent()));
    }

    public Instant timeStamp() {
        return this.signedBody.timeStamp();
    }

    public long salt() {
        return this.signedBody.salt();
    }

    public boolean hasExpiredServer(Instant $$0) {
        return $$0.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_SERVER));
    }

    public boolean hasExpiredClient(Instant $$0) {
        return $$0.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_CLIENT));
    }

    public UUID sender() {
        return this.link.sender();
    }

    public boolean isSystem() {
        return this.sender().equals(SYSTEM_SENDER);
    }

    public boolean hasSignature() {
        return this.signature != null;
    }

    public boolean hasSignatureFrom(UUID $$0) {
        return this.hasSignature() && this.link.sender().equals($$0);
    }

    public boolean isFullyFiltered() {
        return this.filterMask.isFullyFiltered();
    }

    public static String describeSigned(PlayerChatMessage $$02) {
        return "'" + $$02.signedBody.content() + "' @ " + String.valueOf($$02.signedBody.timeStamp()) + "\n - From: " + String.valueOf($$02.link.sender()) + "/" + String.valueOf($$02.link.sessionId()) + ", message #" + $$02.link.index() + "\n - Salt: " + $$02.signedBody.salt() + "\n - Signature: " + MessageSignature.describe($$02.signature) + "\n - Last Seen: [\n" + $$02.signedBody.lastSeen().entries().stream().map($$0 -> "     " + MessageSignature.describe($$0) + "\n").collect(Collectors.joining()) + " ]\n";
    }
}

