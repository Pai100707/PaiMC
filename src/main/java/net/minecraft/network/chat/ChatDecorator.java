package net.minecraft.network.chat;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ChatDecorator {
   ChatDecorator PLAIN = ($$0, $$1) -> $$1;

   Component decorate(ServerPlayer var1, Component var2);
}
