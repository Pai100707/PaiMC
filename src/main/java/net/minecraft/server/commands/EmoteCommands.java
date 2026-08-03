package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.players.PlayerList;

public class EmoteCommands {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register((LiteralArgumentBuilder)Commands.literal("me").then(Commands.argument("action", MessageArgument.message()).executes($$0x -> {
         MessageArgument.resolveChatMessage($$0x, "action", $$1 -> {
            CommandSourceStack $$2 = (CommandSourceStack)$$0x.getSource();
            PlayerList $$3 = $$2.getServer().getPlayerList();
            $$3.broadcastChatMessage($$1, $$2, ChatType.bind(ChatType.EMOTE_COMMAND, $$2));
         });
         return 1;
      })));
   }
}
