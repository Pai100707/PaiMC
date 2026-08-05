/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.players.PlayerList;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)Commands.literal("me").then(Commands.argument("action", MessageArgument.message()).executes($$0 -> {
            MessageArgument.resolveChatMessage((CommandContext<CommandSourceStack>)$$0, "action", $$1 -> {
                CommandSourceStack $$2 = (CommandSourceStack)$$0.getSource();
                PlayerList $$3 = $$2.getServer().getPlayerList();
                $$3.broadcastChatMessage((PlayerChatMessage)$$1, $$2, ChatType.bind(ChatType.EMOTE_COMMAND, $$2));
            });
            return 1;
        })));
    }
}

