/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record RemoteChatSession(UUID sessionId, ProfilePublicKey profilePublicKey) {
    public SignedMessageValidator createMessageValidator(Duration $$0) {
        return new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator(), () -> this.profilePublicKey.data().hasExpired($$0));
    }

    public SignedMessageChain.Decoder createMessageDecoder(UUID $$0) {
        return new SignedMessageChain($$0, this.sessionId).decoder(this.profilePublicKey);
    }

    public Data asData() {
        return new Data(this.sessionId, this.profilePublicKey.data());
    }

    public boolean hasExpired() {
        return this.profilePublicKey.data().hasExpired();
    }

    public record Data(UUID sessionId, ProfilePublicKey.Data profilePublicKey) {
        public static Data read(FriendlyByteBuf $$0) {
            return new Data($$0.readUUID(), new ProfilePublicKey.Data($$0));
        }

        public static void write(FriendlyByteBuf $$0, Data $$1) {
            $$0.writeUUID($$1.sessionId);
            $$1.profilePublicKey.write($$0);
        }

        public RemoteChatSession validate(GameProfile $$0, SignatureValidator $$1) throws ProfilePublicKey.ValidationException {
            return new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated($$1, $$0.id(), this.profilePublicKey));
        }
    }
}

