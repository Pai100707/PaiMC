/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;

public class WeatherCommand {
    private static final int DEFAULT_TIME = -1;

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("weather").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((LiteralArgumentBuilder)Commands.literal("clear").executes($$0 -> WeatherCommand.setClear((CommandSourceStack)$$0.getSource(), -1))).then(Commands.argument("duration", TimeArgument.time(1)).executes($$0 -> WeatherCommand.setClear((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"duration")))))).then(((LiteralArgumentBuilder)Commands.literal("rain").executes($$0 -> WeatherCommand.setRain((CommandSourceStack)$$0.getSource(), -1))).then(Commands.argument("duration", TimeArgument.time(1)).executes($$0 -> WeatherCommand.setRain((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"duration")))))).then(((LiteralArgumentBuilder)Commands.literal("thunder").executes($$0 -> WeatherCommand.setThunder((CommandSourceStack)$$0.getSource(), -1))).then(Commands.argument("duration", TimeArgument.time(1)).executes($$0 -> WeatherCommand.setThunder((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"duration"))))));
    }

    private static int getDuration(CommandSourceStack $$0, int $$1, IntProvider $$2) {
        if ($$1 == -1) {
            return $$2.sample($$0.getServer().overworld().getRandom());
        }
        return $$1;
    }

    private static int setClear(CommandSourceStack $$0, int $$1) {
        $$0.getServer().overworld().setWeatherParameters(WeatherCommand.getDuration($$0, $$1, ServerLevel.RAIN_DELAY), 0, false, false);
        $$0.sendSuccess(() -> Component.translatable("commands.weather.set.clear"), true);
        return $$1;
    }

    private static int setRain(CommandSourceStack $$0, int $$1) {
        $$0.getServer().overworld().setWeatherParameters(0, WeatherCommand.getDuration($$0, $$1, ServerLevel.RAIN_DURATION), true, false);
        $$0.sendSuccess(() -> Component.translatable("commands.weather.set.rain"), true);
        return $$1;
    }

    private static int setThunder(CommandSourceStack $$0, int $$1) {
        $$0.getServer().overworld().setWeatherParameters(0, WeatherCommand.getDuration($$0, $$1, ServerLevel.THUNDER_DURATION), true, true);
        $$0.sendSuccess(() -> Component.translatable("commands.weather.set.thunder"), true);
        return $$1;
    }
}

