/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;

public class WardenSpawnTrackerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("warden_spawn_tracker").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("clear").executes($$0 -> WardenSpawnTrackerCommand.resetTracker((CommandSourceStack)$$0.getSource(), (Collection<? extends Player>)ImmutableList.of((Object)((CommandSourceStack)$$0.getSource()).getPlayerOrException()))))).then(Commands.literal("set").then(Commands.argument("warning_level", IntegerArgumentType.integer((int)0, (int)4)).executes($$0 -> WardenSpawnTrackerCommand.setWarningLevel((CommandSourceStack)$$0.getSource(), (Collection<? extends Player>)ImmutableList.of((Object)((CommandSourceStack)$$0.getSource()).getPlayerOrException()), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"warning_level"))))));
    }

    private static int setWarningLevel(CommandSourceStack $$0, Collection<? extends Player> $$12, int $$2) {
        for (Player player : $$12) {
            player.getWardenSpawnTracker().ifPresent($$1 -> $$1.setWarningLevel($$2));
        }
        if ($$12.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.set.success.single", ((Player)$$12.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.set.success.multiple", $$12.size()), true);
        }
        return $$12.size();
    }

    private static int resetTracker(CommandSourceStack $$0, Collection<? extends Player> $$1) {
        for (Player player : $$1) {
            player.getWardenSpawnTracker().ifPresent(WardenSpawnTracker::reset);
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.clear.success.single", ((Player)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.clear.success.multiple", $$1.size()), true);
        }
        return $$1.size();
    }
}

