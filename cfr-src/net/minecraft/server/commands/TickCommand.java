/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.util.TimeUtil;

public class TickCommand {
    private static final float MAX_TICKRATE = 10000.0f;
    private static final String DEFAULT_TICKRATE = String.valueOf(20);

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tick").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(Commands.literal("query").executes($$0 -> TickCommand.tickQuery((CommandSourceStack)$$0.getSource())))).then(Commands.literal("rate").then(Commands.argument("rate", FloatArgumentType.floatArg((float)1.0f, (float)10000.0f)).suggests(($$0, $$1) -> SharedSuggestionProvider.suggest(new String[]{DEFAULT_TICKRATE}, $$1)).executes($$0 -> TickCommand.setTickingRate((CommandSourceStack)$$0.getSource(), FloatArgumentType.getFloat((CommandContext)$$0, (String)"rate")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("step").executes($$0 -> TickCommand.step((CommandSourceStack)$$0.getSource(), 1))).then(Commands.literal("stop").executes($$0 -> TickCommand.stopStepping((CommandSourceStack)$$0.getSource())))).then(Commands.argument("time", TimeArgument.time(1)).suggests(($$0, $$1) -> SharedSuggestionProvider.suggest(new String[]{"1t", "1s"}, $$1)).executes($$0 -> TickCommand.step((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"time")))))).then(((LiteralArgumentBuilder)Commands.literal("sprint").then(Commands.literal("stop").executes($$0 -> TickCommand.stopSprinting((CommandSourceStack)$$0.getSource())))).then(Commands.argument("time", TimeArgument.time(1)).suggests(($$0, $$1) -> SharedSuggestionProvider.suggest(new String[]{"60s", "1d", "3d"}, $$1)).executes($$0 -> TickCommand.sprint((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"time")))))).then(Commands.literal("unfreeze").executes($$0 -> TickCommand.setFreeze((CommandSourceStack)$$0.getSource(), false)))).then(Commands.literal("freeze").executes($$0 -> TickCommand.setFreeze((CommandSourceStack)$$0.getSource(), true))));
    }

    private static String nanosToMilisString(long $$0) {
        return String.format(Locale.ROOT, "%.1f", Float.valueOf((float)$$0 / (float)TimeUtil.NANOSECONDS_PER_MILLISECOND));
    }

    private static int setTickingRate(CommandSourceStack $$0, float $$1) {
        ServerTickRateManager $$2 = $$0.getServer().tickRateManager();
        $$2.setTickRate($$1);
        String $$3 = String.format(Locale.ROOT, "%.1f", Float.valueOf($$1));
        $$0.sendSuccess(() -> Component.translatable("commands.tick.rate.success", $$3), true);
        return (int)$$1;
    }

    private static int tickQuery(CommandSourceStack $$0) {
        ServerTickRateManager $$1 = $$0.getServer().tickRateManager();
        String $$2 = TickCommand.nanosToMilisString($$0.getServer().getAverageTickTimeNanos());
        float $$3 = $$1.tickrate();
        String $$4 = String.format(Locale.ROOT, "%.1f", Float.valueOf($$3));
        if ($$1.isSprinting()) {
            $$0.sendSuccess(() -> Component.translatable("commands.tick.status.sprinting"), false);
            $$0.sendSuccess(() -> Component.translatable("commands.tick.query.rate.sprinting", $$4, $$2), false);
        } else {
            if ($$1.isFrozen()) {
                $$0.sendSuccess(() -> Component.translatable("commands.tick.status.frozen"), false);
            } else if ($$1.nanosecondsPerTick() < $$0.getServer().getAverageTickTimeNanos()) {
                $$0.sendSuccess(() -> Component.translatable("commands.tick.status.lagging"), false);
            } else {
                $$0.sendSuccess(() -> Component.translatable("commands.tick.status.running"), false);
            }
            String $$5 = TickCommand.nanosToMilisString($$1.nanosecondsPerTick());
            $$0.sendSuccess(() -> Component.translatable("commands.tick.query.rate.running", $$4, $$2, $$5), false);
        }
        long[] $$6 = Arrays.copyOf($$0.getServer().getTickTimesNanos(), $$0.getServer().getTickTimesNanos().length);
        Arrays.sort($$6);
        String $$7 = TickCommand.nanosToMilisString($$6[$$6.length / 2]);
        String $$8 = TickCommand.nanosToMilisString($$6[(int)((double)$$6.length * 0.95)]);
        String $$9 = TickCommand.nanosToMilisString($$6[(int)((double)$$6.length * 0.99)]);
        $$0.sendSuccess(() -> Component.translatable("commands.tick.query.percentiles", $$7, $$8, $$9, $$6.length), false);
        return (int)$$3;
    }

    private static int sprint(CommandSourceStack $$0, int $$1) {
        boolean $$2 = $$0.getServer().tickRateManager().requestGameToSprint($$1);
        if ($$2) {
            $$0.sendSuccess(() -> Component.translatable("commands.tick.sprint.stop.success"), true);
        }
        $$0.sendSuccess(() -> Component.translatable("commands.tick.status.sprinting"), true);
        return 1;
    }

    private static int setFreeze(CommandSourceStack $$0, boolean $$1) {
        ServerTickRateManager $$2 = $$0.getServer().tickRateManager();
        if ($$1) {
            if ($$2.isSprinting()) {
                $$2.stopSprinting();
            }
            if ($$2.isSteppingForward()) {
                $$2.stopStepping();
            }
        }
        $$2.setFrozen($$1);
        if ($$1) {
            $$0.sendSuccess(() -> Component.translatable("commands.tick.status.frozen"), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.tick.status.running"), true);
        }
        return $$1 ? 1 : 0;
    }

    private static int step(CommandSourceStack $$0, int $$1) {
        ServerTickRateManager $$2 = $$0.getServer().tickRateManager();
        boolean $$3 = $$2.stepGameIfPaused($$1);
        if ($$3) {
            $$0.sendSuccess(() -> Component.translatable("commands.tick.step.success", $$1), true);
        } else {
            $$0.sendFailure(Component.translatable("commands.tick.step.fail"));
        }
        return 1;
    }

    private static int stopStepping(CommandSourceStack $$0) {
        ServerTickRateManager $$1 = $$0.getServer().tickRateManager();
        boolean $$2 = $$1.stopStepping();
        if ($$2) {
            $$0.sendSuccess(() -> Component.translatable("commands.tick.step.stop.success"), true);
            return 1;
        }
        $$0.sendFailure(Component.translatable("commands.tick.step.stop.fail"));
        return 0;
    }

    private static int stopSprinting(CommandSourceStack $$0) {
        ServerTickRateManager $$1 = $$0.getServer().tickRateManager();
        boolean $$2 = $$1.stopSprinting();
        if ($$2) {
            $$0.sendSuccess(() -> Component.translatable("commands.tick.sprint.stop.success"), true);
            return 1;
        }
        $$0.sendFailure(Component.translatable("commands.tick.sprint.stop.fail"));
        return 0;
    }
}

