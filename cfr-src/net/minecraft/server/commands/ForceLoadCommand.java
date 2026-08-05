/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  it.unimi.dsi.fastutil.longs.LongSet
 */
package net.minecraft.server.commands;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ForceLoadCommand {
    private static final int MAX_CHUNK_LIMIT = 256;
    private static final Dynamic2CommandExceptionType ERROR_TOO_MANY_CHUNKS = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.forceload.toobig", $$0, $$1));
    private static final Dynamic2CommandExceptionType ERROR_NOT_TICKING = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.forceload.query.failure", $$0, $$1));
    private static final SimpleCommandExceptionType ERROR_ALL_ADDED = new SimpleCommandExceptionType((Message)Component.translatable("commands.forceload.added.failure"));
    private static final SimpleCommandExceptionType ERROR_NONE_REMOVED = new SimpleCommandExceptionType((Message)Component.translatable("commands.forceload.removed.failure"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("forceload").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("add").then(((RequiredArgumentBuilder)Commands.argument("from", ColumnPosArgument.columnPos()).executes($$0 -> ForceLoadCommand.changeForceLoad((CommandSourceStack)$$0.getSource(), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)$$0, "from"), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)$$0, "from"), true))).then(Commands.argument("to", ColumnPosArgument.columnPos()).executes($$0 -> ForceLoadCommand.changeForceLoad((CommandSourceStack)$$0.getSource(), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)$$0, "from"), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)$$0, "to"), true)))))).then(((LiteralArgumentBuilder)Commands.literal("remove").then(((RequiredArgumentBuilder)Commands.argument("from", ColumnPosArgument.columnPos()).executes($$0 -> ForceLoadCommand.changeForceLoad((CommandSourceStack)$$0.getSource(), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)$$0, "from"), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)$$0, "from"), false))).then(Commands.argument("to", ColumnPosArgument.columnPos()).executes($$0 -> ForceLoadCommand.changeForceLoad((CommandSourceStack)$$0.getSource(), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)$$0, "from"), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)$$0, "to"), false))))).then(Commands.literal("all").executes($$0 -> ForceLoadCommand.removeAll((CommandSourceStack)$$0.getSource()))))).then(((LiteralArgumentBuilder)Commands.literal("query").executes($$0 -> ForceLoadCommand.listForceLoad((CommandSourceStack)$$0.getSource()))).then(Commands.argument("pos", ColumnPosArgument.columnPos()).executes($$0 -> ForceLoadCommand.queryForceLoad((CommandSourceStack)$$0.getSource(), ColumnPosArgument.getColumnPos((CommandContext<CommandSourceStack>)$$0, "pos"))))));
    }

    private static int queryForceLoad(CommandSourceStack $$0, ColumnPos $$1) throws CommandSyntaxException {
        ChunkPos $$2 = $$1.toChunkPos();
        ServerLevel $$3 = $$0.getLevel();
        ResourceKey<Level> $$4 = $$3.dimension();
        boolean $$5 = $$3.getForceLoadedChunks().contains($$2.toLong());
        if ($$5) {
            $$0.sendSuccess(() -> Component.translatable("commands.forceload.query.success", Component.translationArg($$2), Component.translationArg($$4.identifier())), false);
            return 1;
        }
        throw ERROR_NOT_TICKING.create((Object)$$2, (Object)$$4.identifier());
    }

    private static int listForceLoad(CommandSourceStack $$0) {
        ServerLevel $$1 = $$0.getLevel();
        ResourceKey<Level> $$2 = $$1.dimension();
        LongSet $$3 = $$1.getForceLoadedChunks();
        int $$4 = $$3.size();
        if ($$4 > 0) {
            String $$5 = Joiner.on((String)", ").join($$3.stream().sorted().map(ChunkPos::new).map(ChunkPos::toString).iterator());
            if ($$4 == 1) {
                $$0.sendSuccess(() -> Component.translatable("commands.forceload.list.single", Component.translationArg($$2.identifier()), $$5), false);
            } else {
                $$0.sendSuccess(() -> Component.translatable("commands.forceload.list.multiple", $$4, Component.translationArg($$2.identifier()), $$5), false);
            }
        } else {
            $$0.sendFailure(Component.translatable("commands.forceload.added.none", Component.translationArg($$2.identifier())));
        }
        return $$4;
    }

    private static int removeAll(CommandSourceStack $$0) {
        ServerLevel $$12 = $$0.getLevel();
        ResourceKey<Level> $$2 = $$12.dimension();
        LongSet $$3 = $$12.getForceLoadedChunks();
        $$3.forEach($$1 -> $$12.setChunkForced(ChunkPos.getX($$1), ChunkPos.getZ($$1), false));
        $$0.sendSuccess(() -> Component.translatable("commands.forceload.removed.all", Component.translationArg($$2.identifier())), true);
        return 0;
    }

    private static int changeForceLoad(CommandSourceStack $$0, ColumnPos $$1, ColumnPos $$2, boolean $$3) throws CommandSyntaxException {
        int $$11;
        int $$4 = Math.min($$1.x(), $$2.x());
        int $$5 = Math.min($$1.z(), $$2.z());
        int $$6 = Math.max($$1.x(), $$2.x());
        int $$7 = Math.max($$1.z(), $$2.z());
        if ($$4 < -30000000 || $$5 < -30000000 || $$6 >= 30000000 || $$7 >= 30000000) {
            throw BlockPosArgument.ERROR_OUT_OF_WORLD.create();
        }
        int $$8 = SectionPos.blockToSectionCoord($$4);
        int $$9 = SectionPos.blockToSectionCoord($$5);
        int $$10 = SectionPos.blockToSectionCoord($$6);
        long $$12 = ((long)($$10 - $$8) + 1L) * ((long)(($$11 = SectionPos.blockToSectionCoord($$7)) - $$9) + 1L);
        if ($$12 > 256L) {
            throw ERROR_TOO_MANY_CHUNKS.create((Object)256, (Object)$$12);
        }
        ServerLevel $$13 = $$0.getLevel();
        ResourceKey<Level> $$14 = $$13.dimension();
        ChunkPos $$15 = null;
        int $$16 = 0;
        for (int $$17 = $$8; $$17 <= $$10; ++$$17) {
            for (int $$18 = $$9; $$18 <= $$11; ++$$18) {
                boolean $$19 = $$13.setChunkForced($$17, $$18, $$3);
                if (!$$19) continue;
                ++$$16;
                if ($$15 != null) continue;
                $$15 = new ChunkPos($$17, $$18);
            }
        }
        ChunkPos $$20 = $$15;
        int $$21 = $$16;
        if ($$21 == 0) {
            throw ($$3 ? ERROR_ALL_ADDED : ERROR_NONE_REMOVED).create();
        }
        if ($$21 == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.forceload." + ($$3 ? "added" : "removed") + ".single", Component.translationArg($$20), Component.translationArg($$14.identifier())), true);
        } else {
            ChunkPos $$22 = new ChunkPos($$8, $$9);
            ChunkPos $$23 = new ChunkPos($$10, $$11);
            $$0.sendSuccess(() -> Component.translatable("commands.forceload." + ($$3 ? "added" : "removed") + ".multiple", $$21, Component.translationArg($$14.identifier()), Component.translationArg($$22), Component.translationArg($$23)), true);
        }
        return $$21;
    }
}

