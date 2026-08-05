/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;

public class GameModeCommand {
    public static final PermissionCheck PERMISSION_CHECK = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("gamemode").requires(Commands.hasPermission(PERMISSION_CHECK))).then(((RequiredArgumentBuilder)Commands.argument("gamemode", GameModeArgument.gameMode()).executes($$0 -> GameModeCommand.setMode((CommandContext<CommandSourceStack>)$$0, Collections.singleton(((CommandSourceStack)$$0.getSource()).getPlayerOrException()), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)$$0, "gamemode")))).then(Commands.argument("target", EntityArgument.players()).executes($$0 -> GameModeCommand.setMode((CommandContext<CommandSourceStack>)$$0, EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "target"), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)$$0, "gamemode"))))));
    }

    private static void logGamemodeChange(CommandSourceStack $$0, ServerPlayer $$1, GameType $$2) {
        MutableComponent $$3 = Component.translatable("gameMode." + $$2.getName());
        if ($$0.getEntity() == $$1) {
            $$0.sendSuccess(() -> Component.translatable("commands.gamemode.success.self", $$3), true);
        } else {
            if ($$0.getLevel().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).booleanValue()) {
                $$1.sendSystemMessage(Component.translatable("gameMode.changed", $$3));
            }
            $$0.sendSuccess(() -> Component.translatable("commands.gamemode.success.other", $$1.getDisplayName(), $$3), true);
        }
    }

    private static int setMode(CommandContext<CommandSourceStack> $$0, Collection<ServerPlayer> $$1, GameType $$2) {
        int $$3 = 0;
        for (ServerPlayer $$4 : $$1) {
            if (!GameModeCommand.setGameMode((CommandSourceStack)$$0.getSource(), $$4, $$2)) continue;
            ++$$3;
        }
        return $$3;
    }

    public static void setGameMode(ServerPlayer $$0, GameType $$1) {
        GameModeCommand.setGameMode($$0.createCommandSourceStack(), $$0, $$1);
    }

    private static boolean setGameMode(CommandSourceStack $$0, ServerPlayer $$1, GameType $$2) {
        if ($$1.setGameMode($$2)) {
            GameModeCommand.logGamemodeChange($$0, $$1, $$2);
            return true;
        }
        return false;
    }
}

