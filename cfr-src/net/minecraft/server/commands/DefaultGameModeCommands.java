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
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;

public class DefaultGameModeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("defaultgamemode").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.argument("gamemode", GameModeArgument.gameMode()).executes($$0 -> DefaultGameModeCommands.setMode((CommandSourceStack)$$0.getSource(), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)$$0, "gamemode")))));
    }

    private static int setMode(CommandSourceStack $$0, GameType $$1) {
        MinecraftServer $$2 = $$0.getServer();
        $$2.setDefaultGameType($$1);
        int $$3 = $$2.enforceGameTypeForPlayers($$2.getForcedGameType());
        $$0.sendSuccess(() -> Component.translatable("commands.defaultgamemode.success", $$1.getLongDisplayName()), true);
        return $$3;
    }
}

