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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.LookAt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;

public class RotateCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("rotate").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((RequiredArgumentBuilder)Commands.argument("target", EntityArgument.entity()).then(Commands.argument("rotation", RotationArgument.rotation()).executes($$0 -> RotateCommand.rotate((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), RotationArgument.getRotation((CommandContext<CommandSourceStack>)$$0, "rotation"))))).then(((LiteralArgumentBuilder)Commands.literal("facing").then(Commands.literal("entity").then(((RequiredArgumentBuilder)Commands.argument("facingEntity", EntityArgument.entity()).executes($$0 -> RotateCommand.rotate((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), new LookAt.LookAtEntity(EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "facingEntity"), EntityAnchorArgument.Anchor.FEET)))).then(Commands.argument("facingAnchor", EntityAnchorArgument.anchor()).executes($$0 -> RotateCommand.rotate((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), new LookAt.LookAtEntity(EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "facingEntity"), EntityAnchorArgument.getAnchor((CommandContext<CommandSourceStack>)$$0, "facingAnchor")))))))).then(Commands.argument("facingLocation", Vec3Argument.vec3()).executes($$0 -> RotateCommand.rotate((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), new LookAt.LookAtPosition(Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "facingLocation"))))))));
    }

    private static int rotate(CommandSourceStack $$0, Entity $$1, Coordinates $$2) {
        Vec2 $$3 = $$2.getRotation($$0);
        float $$4 = $$2.isYRelative() ? $$3.y - $$1.getYRot() : $$3.y;
        float $$5 = $$2.isXRelative() ? $$3.x - $$1.getXRot() : $$3.x;
        $$1.forceSetRotation($$4, $$2.isYRelative(), $$5, $$2.isXRelative());
        $$0.sendSuccess(() -> Component.translatable("commands.rotate.success", $$1.getDisplayName()), true);
        return 1;
    }

    private static int rotate(CommandSourceStack $$0, Entity $$1, LookAt $$2) {
        $$2.perform($$0, $$1);
        $$0.sendSuccess(() -> Component.translatable("commands.rotate.success", $$1.getDisplayName()), true);
        return 1;
    }
}

