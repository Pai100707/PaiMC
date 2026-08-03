package net.minecraft.network.chat;

import net.minecraft.server.level.ServerPlayer;

public interface OutgoingChatMessage {
   Component content();

   void sendToPlayer(ServerPlayer var1, boolean var2, ChatType.Bound var3);

   static OutgoingChatMessage create(PlayerChatMessage $$0) {
      return (OutgoingChatMessage)($$0.isSystem() ? new OutgoingChatMessage.Disguised($$0.decoratedContent()) : new OutgoingChatMessage.Player($$0));
   }

   public record Disguised(Component content) implements OutgoingChatMessage {
      @Override
      public void sendToPlayer(ServerPlayer $$0, boolean $$1, ChatType.Bound $$2) {
         $$0.connection.sendDisguisedChatMessage(this.content, $$2);
      }
   }

   public record Player(PlayerChatMessage message) implements OutgoingChatMessage {
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
