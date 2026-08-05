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
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.LookAt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TeleportCommand {
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType((Message)Component.translatable("commands.teleport.invalidPosition"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        LiteralCommandNode $$1 = $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("teleport").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.argument("location", Vec3Argument.vec3()).executes($$0 -> TeleportCommand.teleportToPos((CommandSourceStack)$$0.getSource(), Collections.singleton(((CommandSourceStack)$$0.getSource()).getEntityOrException()), ((CommandSourceStack)$$0.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)$$0, "location"), null, null)))).then(Commands.argument("destination", EntityArgument.entity()).executes($$0 -> TeleportCommand.teleportToEntity((CommandSourceStack)$$0.getSource(), Collections.singleton(((CommandSourceStack)$$0.getSource()).getEntityOrException()), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "destination"))))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("location", Vec3Argument.vec3()).executes($$0 -> TeleportCommand.teleportToPos((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), ((CommandSourceStack)$$0.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)$$0, "location"), null, null))).then(Commands.argument("rotation", RotationArgument.rotation()).executes($$0 -> TeleportCommand.teleportToPos((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), ((CommandSourceStack)$$0.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)$$0, "location"), RotationArgument.getRotation((CommandContext<CommandSourceStack>)$$0, "rotation"), null)))).then(((LiteralArgumentBuilder)Commands.literal("facing").then(Commands.literal("entity").then(((RequiredArgumentBuilder)Commands.argument("facingEntity", EntityArgument.entity()).executes($$0 -> TeleportCommand.teleportToPos((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), ((CommandSourceStack)$$0.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)$$0, "location"), null, new LookAt.LookAtEntity(EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "facingEntity"), EntityAnchorArgument.Anchor.FEET)))).then(Commands.argument("facingAnchor", EntityAnchorArgument.anchor()).executes($$0 -> TeleportCommand.teleportToPos((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), ((CommandSourceStack)$$0.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)$$0, "location"), null, new LookAt.LookAtEntity(EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "facingEntity"), EntityAnchorArgument.getAnchor((CommandContext<CommandSourceStack>)$$0, "facingAnchor")))))))).then(Commands.argument("facingLocation", Vec3Argument.vec3()).executes($$0 -> TeleportCommand.teleportToPos((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), ((CommandSourceStack)$$0.getSource()).getLevel(), Vec3Argument.getCoordinates((CommandContext<CommandSourceStack>)$$0, "location"), null, new LookAt.LookAtPosition(Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "facingLocation")))))))).then(Commands.argument("destination", EntityArgument.entity()).executes($$0 -> TeleportCommand.teleportToEntity((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "destination"))))));
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tp").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).redirect((CommandNode)$$1));
    }

    private static int teleportToEntity(CommandSourceStack $$0, Collection<? extends Entity> $$1, Entity $$2) throws CommandSyntaxException {
        for (Entity entity : $$1) {
            TeleportCommand.performTeleport($$0, entity, (ServerLevel)$$2.level(), $$2.getX(), $$2.getY(), $$2.getZ(), EnumSet.noneOf(Relative.class), $$2.getYRot(), $$2.getXRot(), null);
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.single", ((Entity)$$2.iterator().next()).getDisplayName(), $$2.getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.multiple", $$2.size(), $$2.getDisplayName()), true);
        }
        return $$1.size();
    }

    private static int teleportToPos(CommandSourceStack $$0, Collection<? extends Entity> $$1, ServerLevel $$2, Coordinates $$3, @Nullable Coordinates $$4, @Nullable LookAt $$5) throws CommandSyntaxException {
        Vec3 $$6 = $$3.getPosition($$0);
        Vec2 $$7 = $$4 == null ? null : $$4.getRotation($$0);
        for (Entity entity : $$1) {
            Set<Relative> $$9 = TeleportCommand.getRelatives($$3, $$4, entity.level().dimension() == $$2.dimension());
            if ($$7 == null) {
                TeleportCommand.performTeleport($$0, entity, $$2, $$6.x, $$6.y, $$6.z, $$9, entity.getYRot(), entity.getXRot(), $$5);
                continue;
            }
            TeleportCommand.performTeleport($$0, entity, $$2, $$6.x, $$6.y, $$6.z, $$9, $$7.y, $$7.x, $$5);
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.teleport.success.location.single", ((Entity)$$6.iterator().next()).getDisplayName(), TeleportCommand.formatDouble($$1.x), TeleportCommand.formatDouble($$1.y), TeleportCommand.formatDouble($$1.z)), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.teleport.success.location.multiple", $$6.size(), TeleportCommand.formatDouble($$1.x), TeleportCommand.formatDouble($$1.y), TeleportCommand.formatDouble($$1.z)), true);
        }
        return $$1.size();
    }

    private static Set<Relative> getRelatives(Coordinates $$0, @Nullable Coordinates $$1, boolean $$2) {
        Set<Relative> $$3 = Relative.direction($$0.isXRelative(), $$0.isYRelative(), $$0.isZRelative());
        Set $$4 = $$2 ? Relative.position($$0.isXRelative(), $$0.isYRelative(), $$0.isZRelative()) : Set.of();
        Set<Relative> $$5 = $$1 == null ? Relative.ROTATION : Relative.rotation($$1.isYRelative(), $$1.isXRelative());
        return Relative.union($$3, $$4, $$5);
    }

    private static String formatDouble(double $$0) {
        return String.format(Locale.ROOT, "%f", $$0);
    }

    private static void performTeleport(CommandSourceStack $$0, Entity $$1, ServerLevel $$2, double $$3, double $$4, double $$5, Set<Relative> $$6, float $$7, float $$8, @Nullable LookAt $$9) throws CommandSyntaxException {
        LivingEntity $$18;
        float $$17;
        BlockPos $$10 = BlockPos.containing($$3, $$4, $$5);
        if (!Level.isInSpawnableBounds($$10)) {
            throw INVALID_POSITION.create();
        }
        double $$11 = $$6.contains((Object)Relative.X) ? $$3 - $$1.getX() : $$3;
        double $$12 = $$6.contains((Object)Relative.Y) ? $$4 - $$1.getY() : $$4;
        double $$13 = $$6.contains((Object)Relative.Z) ? $$5 - $$1.getZ() : $$5;
        float $$14 = $$6.contains((Object)Relative.Y_ROT) ? $$7 - $$1.getYRot() : $$7;
        float $$15 = $$6.contains((Object)Relative.X_ROT) ? $$8 - $$1.getXRot() : $$8;
        float $$16 = Mth.wrapDegrees($$14);
        if (!$$1.teleportTo($$2, $$11, $$12, $$13, $$6, $$16, $$17 = Mth.wrapDegrees($$15), true)) {
            return;
        }
        if ($$9 != null) {
            $$9.perform($$0, $$1);
        }
        if (!($$1 instanceof LivingEntity) || !($$18 = (LivingEntity)$$1).isFallFlying()) {
            $$1.setDeltaMovement($$1.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            $$1.setOnGround(true);
        }
        if ($$1 instanceof PathfinderMob) {
            PathfinderMob $$19 = (PathfinderMob)$$1;
            $$19.getNavigation().stop();
        }
    }
}

