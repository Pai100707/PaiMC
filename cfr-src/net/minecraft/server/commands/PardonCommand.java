/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;

public class PardonCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType((Message)Component.translatable("commands.pardon.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("pardon").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests(($$0, $$1) -> SharedSuggestionProvider.suggest(((CommandSourceStack)$$0.getSource()).getServer().getPlayerList().getBans().getUserList(), $$1)).executes($$0 -> PardonCommand.pardonPlayers((CommandSourceStack)$$0.getSource(), GameProfileArgument.getGameProfiles((CommandContext<CommandSourceStack>)$$0, "targets")))));
    }

    private static int pardonPlayers(CommandSourceStack $$0, Collection<NameAndId> $$1) throws CommandSyntaxException {
        UserBanList $$2 = $$0.getServer().getPlayerList().getBans();
        int $$3 = 0;
        for (NameAndId $$4 : $$1) {
            if (!$$2.isBanned($$4)) continue;
            $$2.remove($$4);
            ++$$3;
            $$0.sendSuccess(() -> Component.translatable("commands.pardon.success", Component.literal($$4.name())), true);
        }
        if ($$3 == 0) {
            throw ERROR_NOT_BANNED.create();
        }
        return $$3;
    }
}

