/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
    private static final Style SUGGEST_STYLE = Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.type.team.hover"))).withClickEvent(new ClickEvent.SuggestCommand("/teammsg "));
    private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType((Message)Component.translatable("commands.teammsg.failed.noteam"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        LiteralCommandNode $$1 = $$02.register((LiteralArgumentBuilder)Commands.literal("teammsg").then(Commands.argument("message", MessageArgument.message()).executes($$0 -> {
            CommandSourceStack $$1 = (CommandSourceStack)$$0.getSource();
            Entity $$22 = $$1.getEntityOrException();
            PlayerTeam $$3 = $$22.getTeam();
            if ($$3 == null) {
                throw ERROR_NOT_ON_TEAM.create();
            }
            List<ServerPlayer> $$42 = $$1.getServer().getPlayerList().getPlayers().stream().filter($$2 -> $$2 == $$22 || $$2.getTeam() == $$3).toList();
            if (!$$42.isEmpty()) {
                MessageArgument.resolveChatMessage((CommandContext<CommandSourceStack>)$$0, "message", $$4 -> TeamMsgCommand.sendMessage($$1, $$22, $$3, $$42, $$4));
            }
            return $$42.size();
        })));
        $$02.register((LiteralArgumentBuilder)Commands.literal("tm").redirect((CommandNode)$$1));
    }

    private static void sendMessage(CommandSourceStack $$0, Entity $$1, PlayerTeam $$2, List<ServerPlayer> $$3, PlayerChatMessage $$4) {
        MutableComponent $$5 = $$2.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
        ChatType.Bound $$6 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, $$0).withTargetName($$5);
        ChatType.Bound $$7 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_OUTGOING, $$0).withTargetName($$5);
        OutgoingChatMessage $$8 = OutgoingChatMessage.create($$4);
        boolean $$9 = false;
        for (ServerPlayer $$10 : $$3) {
            ChatType.Bound $$11 = $$10 == $$1 ? $$7 : $$6;
            boolean $$12 = $$0.shouldFilterMessageTo($$10);
            $$10.sendChatMessage($$8, $$12, $$11);
            $$9 |= $$12 && $$4.isFullyFiltered();
        }
        if ($$9) {
            $$0.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }
    }
}

