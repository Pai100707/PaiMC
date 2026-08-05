/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;

public class DebugMobSpawningCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$0) {
        LiteralArgumentBuilder $$12 = (LiteralArgumentBuilder)Commands.literal("debugmobspawning").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (MobCategory $$2 : MobCategory.values()) {
            $$12.then(Commands.literal($$2.getName()).then(Commands.argument("at", BlockPosArgument.blockPos()).executes($$1 -> DebugMobSpawningCommand.spawnMobs((CommandSourceStack)$$1.getSource(), $$2, BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$1, "at")))));
        }
        $$0.register($$12);
    }

    private static int spawnMobs(CommandSourceStack $$0, MobCategory $$1, BlockPos $$2) {
        NaturalSpawner.spawnCategoryForPosition($$1, $$0.getLevel(), $$2);
        return 1;
    }
}

