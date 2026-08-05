/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class MsgCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        LiteralCommandNode $$1 = $$02.register((LiteralArgumentBuilder)Commands.literal("msg").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", MessageArgument.message()).executes($$0 -> {
            Collection<ServerPlayer> $$1 = EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets");
            if (!$$1.isEmpty()) {
                MessageArgument.resolveChatMessage((CommandContext<CommandSourceStack>)$$0, "message", $$2 -> MsgCommand.sendMessage((CommandSourceStack)$$0.getSource(), $$1, $$2));
            }
            return $$1.size();
        }))));
        $$02.register((LiteralArgumentBuilder)Commands.literal("tell").redirect((CommandNode)$$1));
        $$02.register((LiteralArgumentBuilder)Commands.literal("w").redirect((CommandNode)$$1));
    }

    private static void sendMessage(CommandSourceStack $$0, Collection<ServerPlayer> $$1, PlayerChatMessage $$2) {
        ChatType.Bound $$3 = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, $$0);
        OutgoingChatMessage $$4 = OutgoingChatMessage.create($$2);
        boolean $$5 = false;
        for (ServerPlayer $$6 : $$1) {
            ChatType.Bound $$7 = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, $$0).withTargetName($$6.getDisplayName());
            $$0.sendChatMessage($$4, false, $$7);
            boolean $$8 = $$0.shouldFilterMessageTo($$6);
            $$6.sendChatMessage($$4, $$8, $$3);
            $$5 |= $$8 && $$2.isFullyFiltered();
        }
        if ($$5) {
            $$0.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }
    }
}

