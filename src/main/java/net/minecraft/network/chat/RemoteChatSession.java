package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.player.ProfilePublicKey.ValidationException;

public record RemoteChatSession(UUID sessionId, ProfilePublicKey profilePublicKey) {
   public SignedMessageValidator createMessageValidator(Duration $$0) {
      return new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator(), () -> this.profilePublicKey.data().hasExpired($$0));
   }

   public SignedMessageChain.Decoder createMessageDecoder(UUID $$0) {
      return new SignedMessageChain($$0, this.sessionId).decoder(this.profilePublicKey);
   }

   public RemoteChatSession.Data asData() {
      return new RemoteChatSession.Data(this.sessionId, this.profilePublicKey.data());
   }

   public boolean hasExpired() {
      return this.profilePublicKey.data().hasExpired();
   }

   public record Data(UUID sessionId, net.minecraft.world.entity.player.ProfilePublicKey.Data profilePublicKey) {
      public static RemoteChatSession.Data read(net.minecraft.network.FriendlyByteBuf $$0) {
         return new RemoteChatSession.Data($$0.readUUID(), new net.minecraft.world.entity.player.ProfilePublicKey.Data($$0));
      }

      public static void write(net.minecraft.network.FriendlyByteBuf $$0, RemoteChatSession.Data $$1) {
         $$0.writeUUID($$1.sessionId);
         $$1.profilePublicKey.write($$0);
      }

      public RemoteChatSession validate(GameProfile $$0, SignatureValidator $$1) throws ValidationException {
         return new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated($$1, $$0.id(), this.profilePublicKey));
      }
   }
}
