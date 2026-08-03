package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SignedMessageChain {
   static final Logger LOGGER = LogUtils.getLogger();
   @Nullable
   SignedMessageLink nextLink;
   Instant lastTimeStamp = Instant.EPOCH;

   public SignedMessageChain(UUID $$0, UUID $$1) {
      this.nextLink = SignedMessageLink.root($$0, $$1);
   }

   public SignedMessageChain.Encoder encoder(Signer $$0) {
      return $$1 -> {
         SignedMessageLink $$2 = this.nextLink;
         if ($$2 == null) {
            return null;
         } else {
            this.nextLink = $$2.advance();
            return new MessageSignature($$0.sign($$2x -> PlayerChatMessage.updateSignature($$2x, $$2, $$1)));
         }
      };
   }

   public SignedMessageChain.Decoder decoder(final ProfilePublicKey $$0) {
      final SignatureValidator $$1 = $$0.createSignatureValidator();
      return new SignedMessageChain.Decoder() {
         @Override
         public PlayerChatMessage unpack(@Nullable MessageSignature $$0x, SignedMessageBody $$1x) throws SignedMessageChain.DecodeException {
            if ($$0 == null) {
               throw new SignedMessageChain.DecodeException(SignedMessageChain.DecodeException.MISSING_PROFILE_KEY);
            } else if ($$0.data().hasExpired()) {
               throw new SignedMessageChain.DecodeException(SignedMessageChain.DecodeException.EXPIRED_PROFILE_KEY);
            } else {
               SignedMessageLink $$2 = SignedMessageChain.this.nextLink;
               if ($$2 == null) {
                  throw new SignedMessageChain.DecodeException(SignedMessageChain.DecodeException.CHAIN_BROKEN);
               } else if ($$1.timeStamp().isBefore(SignedMessageChain.this.lastTimeStamp)) {
                  this.setChainBroken();
                  throw new SignedMessageChain.DecodeException(SignedMessageChain.DecodeException.OUT_OF_ORDER_CHAT);
               } else {
                  SignedMessageChain.this.lastTimeStamp = $$1.timeStamp();
                  PlayerChatMessage $$3 = new PlayerChatMessage($$2, $$0, $$1, null, FilterMask.PASS_THROUGH);
                  if (!$$3.verify($$1)) {
                     this.setChainBroken();
                     throw new SignedMessageChain.DecodeException(SignedMessageChain.DecodeException.INVALID_SIGNATURE);
                  } else {
                     if ($$3.hasExpiredServer(Instant.now())) {
                        SignedMessageChain.LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", $$1.content());
                     }

                     SignedMessageChain.this.nextLink = $$2.advance();
                     return $$3;
                  }
               }
            }
         }

         @Override
         public void setChainBroken() {
            SignedMessageChain.this.nextLink = null;
         }
      };
   }

   public static class DecodeException extends ThrowingComponent {
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
   public interface Decoder {
      static SignedMessageChain.Decoder unsigned(UUID $$0, BooleanSupplier $$1) {
         return ($$2, $$3) -> {
            if ($$1.getAsBoolean()) {
               throw new SignedMessageChain.DecodeException(SignedMessageChain.DecodeException.MISSING_PROFILE_KEY);
            } else {
               return PlayerChatMessage.unsigned($$0, $$3.content());
            }
         };
      }

      PlayerChatMessage unpack(@Nullable MessageSignature var1, SignedMessageBody var2) throws SignedMessageChain.DecodeException;

      default void setChainBroken() {
      }
   }

   @FunctionalInterface
   public interface Encoder {
      SignedMessageChain.Encoder UNSIGNED = $$0 -> null;

      @Nullable
      MessageSignature pack(SignedMessageBody var1);
   }
}
