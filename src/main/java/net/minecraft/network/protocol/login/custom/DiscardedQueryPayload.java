package net.minecraft.network.protocol.login.custom;

import net.minecraft.resources.Identifier;

public record DiscardedQueryPayload(Identifier id) implements CustomQueryPayload {
   @Override
   public void write(net.minecraft.network.FriendlyByteBuf $$0) {
   }
}
