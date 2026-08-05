/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class RideCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_RIDING = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.ride.not_riding", $$0));
    private static final Dynamic2CommandExceptionType ERROR_ALREADY_RIDING = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.ride.already_riding", $$0, $$1));
    private static final Dynamic2CommandExceptionType ERROR_MOUNT_FAILED = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.ride.mount.failure.generic", $$0, $$1));
    private static final SimpleCommandExceptionType ERROR_MOUNTING_PLAYER = new SimpleCommandExceptionType((Message)Component.translatable("commands.ride.mount.failure.cant_ride_players"));
    private static final SimpleCommandExceptionType ERROR_MOUNTING_LOOP = new SimpleCommandExceptionType((Message)Component.translatable("commands.ride.mount.failure.loop"));
    private static final SimpleCommandExceptionType ERROR_WRONG_DIMENSION = new SimpleCommandExceptionType((Message)Component.translatable("commands.ride.mount.failure.wrong_dimension"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("ride").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((RequiredArgumentBuilder)Commands.argument("target", EntityArgument.entity()).then(Commands.literal("mount").then(Commands.argument("vehicle", EntityArgument.entity()).executes($$0 -> RideCommand.mount((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "vehicle")))))).then(Commands.literal("dismount").executes($$0 -> RideCommand.dismount((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"))))));
    }

    private static int mount(CommandSourceStack $$0, Entity $$12, Entity $$2) throws CommandSyntaxException {
        Entity $$3 = $$12.getVehicle();
        if ($$3 != null) {
            throw ERROR_ALREADY_RIDING.create((Object)$$12.getDisplayName(), (Object)$$3.getDisplayName());
        }
        if ($$2.getType() == EntityType.PLAYER) {
            throw ERROR_MOUNTING_PLAYER.create();
        }
        if ($$12.getSelfAndPassengers().anyMatch($$1 -> $$1 == $$2)) {
            throw ERROR_MOUNTING_LOOP.create();
        }
        if ($$12.level() != $$2.level()) {
            throw ERROR_WRONG_DIMENSION.create();
        }
        if (!$$12.startRiding($$2, true, true)) {
            throw ERROR_MOUNT_FAILED.create((Object)$$12.getDisplayName(), (Object)$$2.getDisplayName());
        }
        $$0.sendSuccess(() -> Component.translatable("commands.ride.mount.success", $$12.getDisplayName(), $$2.getDisplayName()), true);
        return 1;
    }

    private static int dismount(CommandSourceStack $$0, Entity $$1) throws CommandSyntaxException {
        Entity $$2 = $$1.getVehicle();
        if ($$2 == null) {
            throw ERROR_NOT_RIDING.create((Object)$$1.getDisplayName());
        }
        $$1.stopRiding();
        $$0.sendSuccess(() -> Component.translatable("commands.ride.dismount.success", $$1.getDisplayName(), $$2.getDisplayName()), true);
        return 1;
    }
}

