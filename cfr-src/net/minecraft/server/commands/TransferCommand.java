/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.server.level.ServerPlayer;

public class TransferCommand {
    private static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType((Message)Component.translatable("commands.transfer.error.no_players"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("transfer").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(((RequiredArgumentBuilder)Commands.argument("hostname", StringArgumentType.string()).executes($$0 -> TransferCommand.transfer((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"hostname"), 25565, List.of(((CommandSourceStack)$$0.getSource()).getPlayerOrException())))).then(((RequiredArgumentBuilder)Commands.argument("port", IntegerArgumentType.integer((int)1, (int)65535)).executes($$0 -> TransferCommand.transfer((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"hostname"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"port"), List.of(((CommandSourceStack)$$0.getSource()).getPlayerOrException())))).then(Commands.argument("players", EntityArgument.players()).executes($$0 -> TransferCommand.transfer((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"hostname"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"port"), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "players")))))));
    }

    private static int transfer(CommandSourceStack $$0, String $$1, int $$2, Collection<ServerPlayer> $$3) throws CommandSyntaxException {
        if ($$3.isEmpty()) {
            throw ERROR_NO_PLAYERS.create();
        }
        for (ServerPlayer $$4 : $$3) {
            $$4.connection.send(new ClientboundTransferPacket($$1, $$2));
        }
        if ($$3.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.transfer.success.single", ((ServerPlayer)$$3.iterator().next()).getDisplayName(), $$1, $$2), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.transfer.success.multiple", $$3.size(), $$1, $$2), true);
        }
        return $$3.size();
    }
}

