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
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jspecify.annotations.Nullable;

public class SetBlockCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.setblock.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$12) {
        Predicate<BlockInWorld> $$2 = $$0 -> $$0.getLevel().isEmptyBlock($$0.getPos());
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setblock").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.argument("pos", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("block", BlockStateArgument.block($$12)).executes($$0 -> SetBlockCommand.setBlock((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)$$0, "block"), Mode.REPLACE, null, false))).then(Commands.literal("destroy").executes($$0 -> SetBlockCommand.setBlock((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)$$0, "block"), Mode.DESTROY, null, false)))).then(Commands.literal("keep").executes($$1 -> SetBlockCommand.setBlock((CommandSourceStack)$$1.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$1, "pos"), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)$$1, "block"), Mode.REPLACE, $$2, false)))).then(Commands.literal("replace").executes($$0 -> SetBlockCommand.setBlock((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)$$0, "block"), Mode.REPLACE, null, false)))).then(Commands.literal("strict").executes($$0 -> SetBlockCommand.setBlock((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)$$0, "block"), Mode.REPLACE, null, true))))));
    }

    private static int setBlock(CommandSourceStack $$0, BlockPos $$1, BlockInput $$2, Mode $$3, @Nullable Predicate<BlockInWorld> $$4, boolean $$5) throws CommandSyntaxException {
        boolean $$8;
        ServerLevel $$6 = $$0.getLevel();
        if ($$6.isDebug()) {
            throw ERROR_FAILED.create();
        }
        if ($$4 != null && !$$4.test(new BlockInWorld($$6, $$1, true))) {
            throw ERROR_FAILED.create();
        }
        if ($$3 == Mode.DESTROY) {
            $$6.destroyBlock($$1, true);
            boolean $$7 = !$$2.getState().isAir() || !$$6.getBlockState($$1).isAir();
        } else {
            $$8 = true;
        }
        BlockState $$9 = $$6.getBlockState($$1);
        if ($$8 && !$$2.place($$6, $$1, 2 | ($$5 ? 816 : 256))) {
            throw ERROR_FAILED.create();
        }
        if (!$$5) {
            $$6.updateNeighboursOnBlockSet($$1, $$9);
        }
        $$0.sendSuccess(() -> Component.translatable("commands.setblock.success", $$1.getX(), $$1.getY(), $$1.getZ()), true);
        return 1;
    }

    public static enum Mode {
        REPLACE,
        DESTROY;

    }
}

