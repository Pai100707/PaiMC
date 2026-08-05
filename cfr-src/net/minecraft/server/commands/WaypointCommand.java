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
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.HexColorArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.WaypointArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;
import net.minecraft.world.waypoints.WaypointTransmitter;

public class WaypointCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$1) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("waypoint").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("list").executes($$0 -> WaypointCommand.listWaypoints((CommandSourceStack)$$0.getSource())))).then(Commands.literal("modify").then(((RequiredArgumentBuilder)Commands.argument("waypoint", EntityArgument.entity()).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("color").then(Commands.argument("color", ColorArgument.color()).executes($$0 -> WaypointCommand.setWaypointColor((CommandSourceStack)$$0.getSource(), WaypointArgument.getWaypoint((CommandContext<CommandSourceStack>)$$0, "waypoint"), ColorArgument.getColor((CommandContext<CommandSourceStack>)$$0, "color"))))).then(Commands.literal("hex").then(Commands.argument("color", HexColorArgument.hexColor()).executes($$0 -> WaypointCommand.setWaypointColor((CommandSourceStack)$$0.getSource(), WaypointArgument.getWaypoint((CommandContext<CommandSourceStack>)$$0, "waypoint"), HexColorArgument.getHexColor((CommandContext<CommandSourceStack>)$$0, "color")))))).then(Commands.literal("reset").executes($$0 -> WaypointCommand.resetWaypointColor((CommandSourceStack)$$0.getSource(), WaypointArgument.getWaypoint((CommandContext<CommandSourceStack>)$$0, "waypoint")))))).then(((LiteralArgumentBuilder)Commands.literal("style").then(Commands.literal("reset").executes($$0 -> WaypointCommand.setWaypointStyle((CommandSourceStack)$$0.getSource(), WaypointArgument.getWaypoint((CommandContext<CommandSourceStack>)$$0, "waypoint"), WaypointStyleAssets.DEFAULT)))).then(Commands.literal("set").then(Commands.argument("style", IdentifierArgument.id()).executes($$0 -> WaypointCommand.setWaypointStyle((CommandSourceStack)$$0.getSource(), WaypointArgument.getWaypoint((CommandContext<CommandSourceStack>)$$0, "waypoint"), ResourceKey.create(WaypointStyleAssets.ROOT_ID, IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "style"))))))))));
    }

    private static int setWaypointStyle(CommandSourceStack $$0, WaypointTransmitter $$12, ResourceKey<WaypointStyleAsset> $$2) {
        WaypointCommand.mutateIcon($$0, $$12, $$1 -> {
            $$1.style = $$2;
        });
        $$0.sendSuccess(() -> Component.translatable("commands.waypoint.modify.style"), false);
        return 0;
    }

    private static int setWaypointColor(CommandSourceStack $$0, WaypointTransmitter $$12, ChatFormatting $$2) {
        WaypointCommand.mutateIcon($$0, $$12, $$1 -> {
            $$1.color = Optional.of($$2.getColor());
        });
        $$0.sendSuccess(() -> Component.translatable("commands.waypoint.modify.color", Component.literal($$2.getName()).withStyle($$2)), false);
        return 0;
    }

    private static int setWaypointColor(CommandSourceStack $$0, WaypointTransmitter $$12, Integer $$2) {
        WaypointCommand.mutateIcon($$0, $$12, $$1 -> {
            $$1.color = Optional.of($$2);
        });
        $$0.sendSuccess(() -> Component.translatable("commands.waypoint.modify.color", Component.literal(HexFormat.of().withUpperCase().toHexDigits(ARGB.color(0, (int)$$2), 6)).withColor($$2)), false);
        return 0;
    }

    private static int resetWaypointColor(CommandSourceStack $$02, WaypointTransmitter $$1) {
        WaypointCommand.mutateIcon($$02, $$1, $$0 -> {
            $$0.color = Optional.empty();
        });
        $$02.sendSuccess(() -> Component.translatable("commands.waypoint.modify.color.reset"), false);
        return 0;
    }

    private static int listWaypoints(CommandSourceStack $$0) {
        ServerLevel $$12 = $$0.getLevel();
        Set<WaypointTransmitter> $$2 = $$12.getWaypointManager().transmitters();
        String $$3 = $$12.dimension().identifier().toString();
        if ($$2.isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.waypoint.list.empty", $$3), false);
            return 0;
        }
        Component $$4 = ComponentUtils.formatList($$2.stream().map($$1 -> {
            if ($$1 instanceof LivingEntity) {
                LivingEntity $$2 = (LivingEntity)$$1;
                BlockPos $$32 = $$2.blockPosition();
                return $$2.getFeedbackDisplayName().copy().withStyle($$3 -> $$3.withClickEvent(new ClickEvent.SuggestCommand("/execute in " + $$3 + " run tp @s " + $$32.getX() + " " + $$32.getY() + " " + $$32.getZ())).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip"))).withColor($$2.waypointIcon().color.orElse(-1)));
            }
            return Component.literal($$1.toString());
        }).toList(), Function.identity());
        $$0.sendSuccess(() -> Component.translatable("commands.waypoint.list.success", $$2.size(), $$3, $$4), false);
        return $$2.size();
    }

    private static void mutateIcon(CommandSourceStack $$0, WaypointTransmitter $$1, Consumer<Waypoint.Icon> $$2) {
        ServerLevel $$3 = $$0.getLevel();
        $$3.getWaypointManager().untrackWaypoint($$1);
        $$2.accept($$1.waypointIcon());
        $$3.getWaypointManager().trackWaypoint($$1);
    }
}

