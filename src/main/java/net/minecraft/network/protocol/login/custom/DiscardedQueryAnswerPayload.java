package net.minecraft.network.protocol.login.custom;

public record DiscardedQueryAnswerPayload() implements CustomQueryAnswerPayload {
   public static final DiscardedQueryAnswerPayload INSTANCE = new DiscardedQueryAnswerPayload();

   @Override
   public void write(net.minecraft.network.FriendlyByteBuf $$0) {
   }
}
