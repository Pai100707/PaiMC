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
import net.minecraft.server.players.PlayerList;

public class OpCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_OP = new SimpleCommandExceptionType((Message)Component.translatable("commands.op.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> $$03) {
        $$03.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("op").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests(($$02, $$12) -> {
            PlayerList $$2 = ((CommandSourceStack)$$02.getSource()).getServer().getPlayerList();
            return SharedSuggestionProvider.suggest($$2.getPlayers().stream().filter($$1 -> !$$2.isOp($$1.nameAndId())).map($$0 -> $$0.getGameProfile().name()), $$12);
        }).executes($$0 -> OpCommand.opPlayers((CommandSourceStack)$$0.getSource(), GameProfileArgument.getGameProfiles((CommandContext<CommandSourceStack>)$$0, "targets")))));
    }

    private static int opPlayers(CommandSourceStack $$0, Collection<NameAndId> $$1) throws CommandSyntaxException {
        PlayerList $$2 = $$0.getServer().getPlayerList();
        int $$3 = 0;
        for (NameAndId $$4 : $$1) {
            if ($$2.isOp($$4)) continue;
            $$2.op($$4);
            ++$$3;
            $$0.sendSuccess(() -> Component.translatable("commands.op.success", $$4.name()), true);
        }
        if ($$3 == 0) {
            throw ERROR_ALREADY_OP.create();
        }
        return $$3;
    }
}

