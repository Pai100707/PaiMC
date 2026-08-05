/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.chat;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

public interface OutgoingChatMessage {
    public Component content();

    public void sendToPlayer(ServerPlayer var1, boolean var2, ChatType.Bound var3);

    public static OutgoingChatMessage create(PlayerChatMessage $$0) {
        if ($$0.isSystem()) {
            return new Disguised($$0.decoratedContent());
        }
        return new Player($$0);
    }

    public record Disguised(Component content) implements OutgoingChatMessage
    {
        @Override
        public void sendToPlayer(ServerPlayer $$0, boolean $$1, ChatType.Bound $$2) {
            $$0.connection.sendDisguisedChatMessage(this.content, $$2);
        }
    }

    public record Player(PlayerChatMessage message) implements OutgoingChatMessage
    {
        @Override
        public Component content() {
            return this.message.decoratedContent();
        }

        @Override
        public void sendToPlayer(ServerPlayer $$0, boolean $$1, ChatType.Bound $$2) {
            PlayerChatMessage $$3 = this.message.filter($$1);
            if (!$$3.isFullyFiltered()) {
                $$0.connection.sendPlayerChatMessage($$3, $$2);
            }
        }
    }
}

