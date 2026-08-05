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
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec2;

public class SetWorldSpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setworldspawn").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes($$0 -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)$$0.getSource(), BlockPos.containing(((CommandSourceStack)$$0.getSource()).getPosition()), WorldCoordinates.ZERO_ROTATION))).then(((RequiredArgumentBuilder)Commands.argument("pos", BlockPosArgument.blockPos()).executes($$0 -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)$$0.getSource(), BlockPosArgument.getSpawnablePos((CommandContext<CommandSourceStack>)$$0, "pos"), WorldCoordinates.ZERO_ROTATION))).then(Commands.argument("rotation", RotationArgument.rotation()).executes($$0 -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)$$0.getSource(), BlockPosArgument.getSpawnablePos((CommandContext<CommandSourceStack>)$$0, "pos"), RotationArgument.getRotation((CommandContext<CommandSourceStack>)$$0, "rotation"))))));
    }

    private static int setSpawn(CommandSourceStack $$0, BlockPos $$1, Coordinates $$2) {
        ServerLevel $$3 = $$0.getLevel();
        Vec2 $$4 = $$2.getRotation($$0);
        float $$5 = $$4.y;
        float $$6 = $$4.x;
        LevelData.RespawnData $$7 = LevelData.RespawnData.of($$3.dimension(), $$1, $$5, $$6);
        $$3.setRespawnData($$7);
        $$0.sendSuccess(() -> Component.translatable("commands.setworldspawn.success", $$1.getX(), $$1.getY(), $$1.getZ(), Float.valueOf($$7.yaw()), Float.valueOf($$7.pitch()), $$3.dimension().identifier().toString()), true);
        return 1;
    }
}

