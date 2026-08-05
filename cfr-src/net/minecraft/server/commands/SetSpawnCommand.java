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
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec2;

public class SetSpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spawnpoint").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes($$0 -> SetSpawnCommand.setSpawn((CommandSourceStack)$$0.getSource(), Collections.singleton(((CommandSourceStack)$$0.getSource()).getPlayerOrException()), BlockPos.containing(((CommandSourceStack)$$0.getSource()).getPosition()), WorldCoordinates.ZERO_ROTATION))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes($$0 -> SetSpawnCommand.setSpawn((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), BlockPos.containing(((CommandSourceStack)$$0.getSource()).getPosition()), WorldCoordinates.ZERO_ROTATION))).then(((RequiredArgumentBuilder)Commands.argument("pos", BlockPosArgument.blockPos()).executes($$0 -> SetSpawnCommand.setSpawn((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), BlockPosArgument.getSpawnablePos((CommandContext<CommandSourceStack>)$$0, "pos"), WorldCoordinates.ZERO_ROTATION))).then(Commands.argument("rotation", RotationArgument.rotation()).executes($$0 -> SetSpawnCommand.setSpawn((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), BlockPosArgument.getSpawnablePos((CommandContext<CommandSourceStack>)$$0, "pos"), RotationArgument.getRotation((CommandContext<CommandSourceStack>)$$0, "rotation")))))));
    }

    private static int setSpawn(CommandSourceStack $$0, Collection<ServerPlayer> $$1, BlockPos $$2, Coordinates $$3) {
        ResourceKey<Level> $$4 = $$0.getLevel().dimension();
        Vec2 $$5 = $$3.getRotation($$0);
        float $$6 = Mth.wrapDegrees($$5.y);
        float $$7 = Mth.clamp($$5.x, -90.0f, 90.0f);
        for (ServerPlayer $$8 : $$1) {
            $$8.setRespawnPosition(new ServerPlayer.RespawnConfig(LevelData.RespawnData.of($$4, $$2, $$6, $$7), true), false);
        }
        String $$9 = $$4.identifier().toString();
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.spawnpoint.success.single", $$2.getX(), $$2.getY(), $$2.getZ(), Float.valueOf($$6), Float.valueOf($$7), $$9, ((ServerPlayer)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.spawnpoint.success.multiple", $$2.getX(), $$2.getY(), $$2.getZ(), Float.valueOf($$6), Float.valueOf($$7), $$9, $$1.size()), true);
        }
        return $$1.size();
    }
}

