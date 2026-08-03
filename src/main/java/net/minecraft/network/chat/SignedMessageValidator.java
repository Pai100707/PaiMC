package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.util.function.BooleanSupplier;
import net.minecraft.util.SignatureValidator;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@FunctionalInterface
public interface SignedMessageValidator {
   Logger LOGGER = LogUtils.getLogger();
   SignedMessageValidator ACCEPT_UNSIGNED = PlayerChatMessage::removeSignature;
   SignedMessageValidator REJECT_ALL = $$0 -> {
      LOGGER.error("Received chat message from {}, but they have no chat session initialized and secure chat is enforced", $$0.sender());
      return null;
   };

   @Nullable
   PlayerChatMessage updateAndValidate(PlayerChatMessage var1);

   public static class KeyBased implements SignedMessageValidator {
      private final SignatureValidator validator;
      private final BooleanSupplier expired;
      @Nullable
      private PlayerChatMessage lastMessage;
      private boolean isChainValid = true;

      public KeyBased(SignatureValidator $$0, BooleanSupplier $$1) {
         this.validator = $$0;
         this.expired = $$1;
      }

      private boolean validateChain(PlayerChatMessage $$0) {
         if ($$0.equals(this.lastMessage)) {
            return true;
         } else if (this.lastMessage != null && !$$0.link().isDescendantOf(this.lastMessage.link())) {
            LOGGER.error(
               "Received out-of-order chat message from {}: expected index > {} for session {}, but was {} for session {}",
               new Object[]{$$0.sender(), this.lastMessage.link().index(), this.lastMessage.link().sessionId(), $$0.link().index(), $$0.link().sessionId()}
            );
            return false;
         } else {
            return true;
         }
      }

      private boolean validate(PlayerChatMessage $$0) {
         if (this.expired.getAsBoolean()) {
            LOGGER.error("Received message with expired profile public key from {} with session {}", $$0.sender(), $$0.link().sessionId());
            return false;
         } else if (!$$0.verify(this.validator)) {
            LOGGER.error(
               "Received message with invalid signature (is the session wrong, or signature cache out of sync?): {}", PlayerChatMessage.describeSigned($$0)
            );
            return false;
         } else {
            return this.validateChain($$0);
         }
      }

      @Nullable
      @Override
      public PlayerChatMessage updateAndValidate(PlayerChatMessage $$0) {
         this.isChainValid = this.isChainValid && this.validate($$0);
         if (!this.isChainValid) {
            return null;
         } else {
            this.lastMessage = $$0;
            return $$0;
         }
      }
   }
}
