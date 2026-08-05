/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public record SignedMessageLink(int index, UUID sender, UUID sessionId) {
    public static final Codec<SignedMessageLink> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("index").forGetter(SignedMessageLink::index), (App)UUIDUtil.CODEC.fieldOf("sender").forGetter(SignedMessageLink::sender), (App)UUIDUtil.CODEC.fieldOf("session_id").forGetter(SignedMessageLink::sessionId)).apply((Applicative)$$0, SignedMessageLink::new));

    public static SignedMessageLink unsigned(UUID $$0) {
        return SignedMessageLink.root($$0, Util.NIL_UUID);
    }

    public static SignedMessageLink root(UUID $$0, UUID $$1) {
        return new SignedMessageLink(0, $$0, $$1);
    }

    public void updateSignature(SignatureUpdater.Output $$0) throws SignatureException {
        $$0.update(UUIDUtil.uuidToByteArray(this.sender));
        $$0.update(UUIDUtil.uuidToByteArray(this.sessionId));
        $$0.update(Ints.toByteArray((int)this.index));
    }

    public boolean isDescendantOf(SignedMessageLink $$0) {
        return this.index > $$0.index() && this.sender.equals($$0.sender()) && this.sessionId.equals($$0.sessionId());
    }

    public @Nullable SignedMessageLink advance() {
        if (this.index == Integer.MAX_VALUE) {
            return null;
        }
        return new SignedMessageLink(this.index + 1, this.sender, this.sessionId);
    }
}

