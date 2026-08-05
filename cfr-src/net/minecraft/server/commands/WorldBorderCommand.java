/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

public class WorldBorderCommand {
    private static final SimpleCommandExceptionType ERROR_SAME_CENTER = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.center.failed"));
    private static final SimpleCommandExceptionType ERROR_SAME_SIZE = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.set.failed.nochange"));
    private static final SimpleCommandExceptionType ERROR_TOO_SMALL = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.set.failed.small"));
    private static final SimpleCommandExceptionType ERROR_TOO_BIG = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.set.failed.big", 5.9999968E7));
    private static final SimpleCommandExceptionType ERROR_TOO_FAR_OUT = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.set.failed.far", 2.9999984E7));
    private static final SimpleCommandExceptionType ERROR_SAME_WARNING_TIME = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.warning.time.failed"));
    private static final SimpleCommandExceptionType ERROR_SAME_WARNING_DISTANCE = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.warning.distance.failed"));
    private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_BUFFER = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.damage.buffer.failed"));
    private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_AMOUNT = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.damage.amount.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("worldborder").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("add").then(((RequiredArgumentBuilder)Commands.argument("distance", DoubleArgumentType.doubleArg((double)-5.9999968E7, (double)5.9999968E7)).executes($$0 -> WorldBorderCommand.setSize((CommandSourceStack)$$0.getSource(), ((CommandSourceStack)$$0.getSource()).getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble((CommandContext)$$0, (String)"distance"), 0L))).then(Commands.argument("time", TimeArgument.time(0)).executes($$0 -> WorldBorderCommand.setSize((CommandSourceStack)$$0.getSource(), ((CommandSourceStack)$$0.getSource()).getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble((CommandContext)$$0, (String)"distance"), ((CommandSourceStack)$$0.getSource()).getLevel().getWorldBorder().getLerpTime() + (long)IntegerArgumentType.getInteger((CommandContext)$$0, (String)"time"))))))).then(Commands.literal("set").then(((RequiredArgumentBuilder)Commands.argument("distance", DoubleArgumentType.doubleArg((double)-5.9999968E7, (double)5.9999968E7)).executes($$0 -> WorldBorderCommand.setSize((CommandSourceStack)$$0.getSource(), DoubleArgumentType.getDouble((CommandContext)$$0, (String)"distance"), 0L))).then(Commands.argument("time", TimeArgument.time(0)).executes($$0 -> WorldBorderCommand.setSize((CommandSourceStack)$$0.getSource(), DoubleArgumentType.getDouble((CommandContext)$$0, (String)"distance"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"time"))))))).then(Commands.literal("center").then(Commands.argument("pos", Vec2Argument.vec2()).executes($$0 -> WorldBorderCommand.setCenter((CommandSourceStack)$$0.getSource(), Vec2Argument.getVec2((CommandContext<CommandSourceStack>)$$0, "pos")))))).then(((LiteralArgumentBuilder)Commands.literal("damage").then(Commands.literal("amount").then(Commands.argument("damagePerBlock", FloatArgumentType.floatArg((float)0.0f)).executes($$0 -> WorldBorderCommand.setDamageAmount((CommandSourceStack)$$0.getSource(), FloatArgumentType.getFloat((CommandContext)$$0, (String)"damagePerBlock")))))).then(Commands.literal("buffer").then(Commands.argument("distance", FloatArgumentType.floatArg((float)0.0f)).executes($$0 -> WorldBorderCommand.setDamageBuffer((CommandSourceStack)$$0.getSource(), FloatArgumentType.getFloat((CommandContext)$$0, (String)"distance"))))))).then(Commands.literal("get").executes($$0 -> WorldBorderCommand.getSize((CommandSourceStack)$$0.getSource())))).then(((LiteralArgumentBuilder)Commands.literal("warning").then(Commands.literal("distance").then(Commands.argument("distance", IntegerArgumentType.integer((int)0)).executes($$0 -> WorldBorderCommand.setWarningDistance((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"distance")))))).then(Commands.literal("time").then(Commands.argument("time", TimeArgument.time(0)).executes($$0 -> WorldBorderCommand.setWarningTime((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"time")))))));
    }

    private static int setDamageBuffer(CommandSourceStack $$0, float $$1) throws CommandSyntaxException {
        WorldBorder $$2 = $$0.getLevel().getWorldBorder();
        if ($$2.getSafeZone() == (double)$$1) {
            throw ERROR_SAME_DAMAGE_BUFFER.create();
        }
        $$2.setSafeZone($$1);
        $$0.sendSuccess(() -> Component.translatable("commands.worldborder.damage.buffer.success", String.format(Locale.ROOT, "%.2f", Float.valueOf($$1))), true);
        return (int)$$1;
    }

    private static int setDamageAmount(CommandSourceStack $$0, float $$1) throws CommandSyntaxException {
        WorldBorder $$2 = $$0.getLevel().getWorldBorder();
        if ($$2.getDamagePerBlock() == (double)$$1) {
            throw ERROR_SAME_DAMAGE_AMOUNT.create();
        }
        $$2.setDamagePerBlock($$1);
        $$0.sendSuccess(() -> Component.translatable("commands.worldborder.damage.amount.success", String.format(Locale.ROOT, "%.2f", Float.valueOf($$1))), true);
        return (int)$$1;
    }

    private static int setWarningTime(CommandSourceStack $$0, int $$1) throws CommandSyntaxException {
        WorldBorder $$2 = $$0.getLevel().getWorldBorder();
        if ($$2.getWarningTime() == $$1) {
            throw ERROR_SAME_WARNING_TIME.create();
        }
        $$2.setWarningTime($$1);
        $$0.sendSuccess(() -> Component.translatable("commands.worldborder.warning.time.success", WorldBorderCommand.formatTicksToSeconds($$1)), true);
        return $$1;
    }

    private static int setWarningDistance(CommandSourceStack $$0, int $$1) throws CommandSyntaxException {
        WorldBorder $$2 = $$0.getLevel().getWorldBorder();
        if ($$2.getWarningBlocks() == $$1) {
            throw ERROR_SAME_WARNING_DISTANCE.create();
        }
        $$2.setWarningBlocks($$1);
        $$0.sendSuccess(() -> Component.translatable("commands.worldborder.warning.distance.success", $$1), true);
        return $$1;
    }

    private static int getSize(CommandSourceStack $$0) {
        double $$1 = $$0.getLevel().getWorldBorder().getSize();
        $$0.sendSuccess(() -> Component.translatable("commands.worldborder.get", String.format(Locale.ROOT, "%.0f", $$1)), false);
        return Mth.floor($$1 + 0.5);
    }

    private static int setCenter(CommandSourceStack $$0, Vec2 $$1) throws CommandSyntaxException {
        WorldBorder $$2 = $$0.getLevel().getWorldBorder();
        if ($$2.getCenterX() == (double)$$1.x && $$2.getCenterZ() == (double)$$1.y) {
            throw ERROR_SAME_CENTER.create();
        }
        if ((double)Math.abs($$1.x) > 2.9999984E7 || (double)Math.abs($$1.y) > 2.9999984E7) {
            throw ERROR_TOO_FAR_OUT.create();
        }
        $$2.setCenter($$1.x, $$1.y);
        $$0.sendSuccess(() -> Component.translatable("commands.worldborder.center.success", String.format(Locale.ROOT, "%.2f", Float.valueOf($$0.x)), String.format(Locale.ROOT, "%.2f", Float.valueOf($$0.y))), true);
        return 0;
    }

    private static int setSize(CommandSourceStack $$0, double $$1, long $$2) throws CommandSyntaxException {
        ServerLevel $$3 = $$0.getLevel();
        WorldBorder $$4 = $$3.getWorldBorder();
        double $$5 = $$4.getSize();
        if ($$5 == $$1) {
            throw ERROR_SAME_SIZE.create();
        }
        if ($$1 < 1.0) {
            throw ERROR_TOO_SMALL.create();
        }
        if ($$1 > 5.9999968E7) {
            throw ERROR_TOO_BIG.create();
        }
        String $$6 = String.format(Locale.ROOT, "%.1f", $$1);
        if ($$2 > 0L) {
            $$4.lerpSizeBetween($$5, $$1, $$2, $$3.getGameTime());
            if ($$1 > $$5) {
                $$0.sendSuccess(() -> Component.translatable("commands.worldborder.set.grow", $$6, WorldBorderCommand.formatTicksToSeconds($$2)), true);
            } else {
                $$0.sendSuccess(() -> Component.translatable("commands.worldborder.set.shrink", $$6, WorldBorderCommand.formatTicksToSeconds($$2)), true);
            }
        } else {
            $$4.setSize($$1);
            $$0.sendSuccess(() -> Component.translatable("commands.worldborder.set.immediate", $$6), true);
        }
        return (int)($$1 - $$5);
    }

    private static String formatTicksToSeconds(long $$0) {
        return String.format(Locale.ROOT, "%.2f", (double)$$0 / 20.0);
    }
}

