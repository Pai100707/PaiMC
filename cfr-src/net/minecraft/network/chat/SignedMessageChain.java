/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SignedMessageChain {
    static final Logger LOGGER = LogUtils.getLogger();
    @Nullable SignedMessageLink nextLink;
    Instant lastTimeStamp = Instant.EPOCH;

    public SignedMessageChain(UUID $$0, UUID $$1) {
        this.nextLink = SignedMessageLink.root($$0, $$1);
    }

    public Encoder encoder(Signer $$0) {
        return $$1 -> {
            SignedMessageLink $$22 = this.nextLink;
            if ($$22 == null) {
                return null;
            }
            this.nextLink = $$22.advance();
            return new MessageSignature($$0.sign($$2 -> PlayerChatMessage.updateSignature($$2, $$22, $$1)));
        };
    }

    public Decoder decoder(final ProfilePublicKey $$0) {
        final SignatureValidator $$1 = $$0.createSignatureValidator();
        return new Decoder(){

            @Override
            public PlayerChatMessage unpack(@Nullable MessageSignature $$02, SignedMessageBody $$12) throws DecodeException {
                if ($$02 == null) {
                    throw new DecodeException(DecodeException.MISSING_PROFILE_KEY);
                }
                if ($$0.data().hasExpired()) {
                    throw new DecodeException(DecodeException.EXPIRED_PROFILE_KEY);
                }
                SignedMessageLink $$2 = SignedMessageChain.this.nextLink;
                if ($$2 == null) {
                    throw new DecodeException(DecodeException.CHAIN_BROKEN);
                }
                if ($$12.timeStamp().isBefore(SignedMessageChain.this.lastTimeStamp)) {
                    this.setChainBroken();
                    throw new DecodeException(DecodeException.OUT_OF_ORDER_CHAT);
                }
                SignedMessageChain.this.lastTimeStamp = $$12.timeStamp();
                PlayerChatMessage $$3 = new PlayerChatMessage($$2, $$02, $$12, null, FilterMask.PASS_THROUGH);
                if (!$$3.verify($$1)) {
                    this.setChainBroken();
                    throw new DecodeException(DecodeException.INVALID_SIGNATURE);
                }
                if ($$3.hasExpiredServer(Instant.now())) {
                    LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", (Object)$$12.content());
                }
                SignedMessageChain.this.nextLink = $$2.advance();
                return $$3;
            }

            @Override
            public void setChainBroken() {
                SignedMessageChain.this.nextLink = null;
            }
        };
    }

    @FunctionalInterface
    public static interface Encoder {
        public static final Encoder UNSIGNED = $$0 -> null;

        public @Nullable MessageSignature pack(SignedMessageBody var1);
    }

    public static class DecodeException
    extends ThrowingComponent {
        static final Component MISSING_PROFILE_KEY = Component.translatable("chat.disabled.missingProfileKey");
        static final Component CHAIN_BROKEN = Component.translatable("chat.disabled.chain_broken");
        static final Component EXPIRED_PROFILE_KEY = Component.translatable("chat.disabled.expiredProfileKey");
        static final Component INVALID_SIGNATURE = Component.translatable("chat.disabled.invalid_signature");
        static final Component OUT_OF_ORDER_CHAT = Component.translatable("chat.disabled.out_of_order_chat");

        public DecodeException(Component $$0) {
            super($$0);
        }
    }

    @FunctionalInterface
    public static interface Decoder {
        public static Decoder unsigned(UUID $$0, BooleanSupplier $$1) {
            return ($$2, $$3) -> {
                if ($$1.getAsBoolean()) {
                    throw new DecodeException(DecodeException.MISSING_PROFILE_KEY);
                }
                return PlayerChatMessage.unsigned($$0, $$3.content());
            };
        }

        public PlayerChatMessage unpack(@Nullable MessageSignature var1, SignedMessageBody var2) throws DecodeException;

        default public void setChainBroken() {
        }
    }
}

