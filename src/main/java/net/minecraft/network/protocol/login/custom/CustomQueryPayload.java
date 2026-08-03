package net.minecraft.network.protocol.login.custom;

import net.minecraft.resources.Identifier;

public interface CustomQueryPayload {
   Identifier id();

   void write(net.minecraft.network.FriendlyByteBuf var1);
}
