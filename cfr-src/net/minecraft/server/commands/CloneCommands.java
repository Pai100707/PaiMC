/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.InCommandFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.ticks.LevelTicks;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class CloneCommands {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType((Message)Component.translatable("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.clone.toobig", $$0, $$1));
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.clone.failed"));
    public static final Predicate<BlockInWorld> FILTER_AIR = $$0 -> !$$0.getState().isAir();

    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$1) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clone").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(CloneCommands.beginEndDestinationAndModeSuffix($$1, $$0 -> ((CommandSourceStack)$$0.getSource()).getLevel()))).then(Commands.literal("from").then(Commands.argument("sourceDimension", DimensionArgument.dimension()).then(CloneCommands.beginEndDestinationAndModeSuffix($$1, $$0 -> DimensionArgument.getDimension((CommandContext<CommandSourceStack>)$$0, "sourceDimension"))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> beginEndDestinationAndModeSuffix(CommandBuildContext $$02, InCommandFunction<CommandContext<CommandSourceStack>, ServerLevel> $$1) {
        return Commands.argument("begin", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("end", BlockPosArgument.blockPos()).then(CloneCommands.destinationAndStrictSuffix($$02, $$1, $$0 -> ((CommandSourceStack)$$0.getSource()).getLevel()))).then(Commands.literal("to").then(Commands.argument("targetDimension", DimensionArgument.dimension()).then(CloneCommands.destinationAndStrictSuffix($$02, $$1, $$0 -> DimensionArgument.getDimension((CommandContext<CommandSourceStack>)$$0, "targetDimension"))))));
    }

    private static DimensionAndPosition getLoadedDimensionAndPosition(CommandContext<CommandSourceStack> $$0, ServerLevel $$1, String $$2) throws CommandSyntaxException {
        BlockPos $$3 = BlockPosArgument.getLoadedBlockPos($$0, $$1, $$2);
        return new DimensionAndPosition($$1, $$3);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> destinationAndStrictSuffix(CommandBuildContext $$0, InCommandFunction<CommandContext<CommandSourceStack>, ServerLevel> $$12, InCommandFunction<CommandContext<CommandSourceStack>, ServerLevel> $$2) {
        InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> $$3 = $$1 -> CloneCommands.getLoadedDimensionAndPosition((CommandContext<CommandSourceStack>)$$1, (ServerLevel)$$12.apply((CommandContext<CommandSourceStack>)$$1), "begin");
        InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> $$4 = $$1 -> CloneCommands.getLoadedDimensionAndPosition((CommandContext<CommandSourceStack>)$$1, (ServerLevel)$$12.apply((CommandContext<CommandSourceStack>)$$1), "end");
        InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> $$5 = $$1 -> CloneCommands.getLoadedDimensionAndPosition((CommandContext<CommandSourceStack>)$$1, (ServerLevel)$$2.apply((CommandContext<CommandSourceStack>)$$1), "destination");
        return CloneCommands.modeSuffix($$0, $$3, $$4, $$5, false, Commands.argument("destination", BlockPosArgument.blockPos())).then(CloneCommands.modeSuffix($$0, $$3, $$4, $$5, true, Commands.literal("strict")));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> modeSuffix(CommandBuildContext $$03, InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> $$1, InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> $$2, InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> $$3, boolean $$42, ArgumentBuilder<CommandSourceStack, ?> $$5) {
        return $$5.executes($$4 -> CloneCommands.clone((CommandSourceStack)$$4.getSource(), (DimensionAndPosition)$$1.apply($$4), (DimensionAndPosition)$$2.apply($$4), (DimensionAndPosition)$$3.apply($$4), $$0 -> true, Mode.NORMAL, $$42)).then(CloneCommands.wrapWithCloneMode($$1, $$2, $$3, $$02 -> $$0 -> true, $$42, Commands.literal("replace"))).then(CloneCommands.wrapWithCloneMode($$1, $$2, $$3, $$0 -> FILTER_AIR, $$42, Commands.literal("masked"))).then(Commands.literal("filtered").then(CloneCommands.wrapWithCloneMode($$1, $$2, $$3, $$0 -> BlockPredicateArgument.getBlockPredicate((CommandContext<CommandSourceStack>)$$0, "filter"), $$42, Commands.argument("filter", BlockPredicateArgument.blockPredicate($$03)))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapWithCloneMode(InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> $$0, InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> $$1, InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> $$2, InCommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> $$3, boolean $$4, ArgumentBuilder<CommandSourceStack, ?> $$52) {
        return $$52.executes($$5 -> CloneCommands.clone((CommandSourceStack)$$5.getSource(), (DimensionAndPosition)$$0.apply($$5), (DimensionAndPosition)$$1.apply($$5), (DimensionAndPosition)$$2.apply($$5), (Predicate)$$3.apply($$5), Mode.NORMAL, $$4)).then(Commands.literal("force").executes($$5 -> CloneCommands.clone((CommandSourceStack)$$5.getSource(), (DimensionAndPosition)$$0.apply($$5), (DimensionAndPosition)$$1.apply($$5), (DimensionAndPosition)$$2.apply($$5), (Predicate)$$3.apply($$5), Mode.FORCE, $$4))).then(Commands.literal("move").executes($$5 -> CloneCommands.clone((CommandSourceStack)$$5.getSource(), (DimensionAndPosition)$$0.apply($$5), (DimensionAndPosition)$$1.apply($$5), (DimensionAndPosition)$$2.apply($$5), (Predicate)$$3.apply($$5), Mode.MOVE, $$4))).then(Commands.literal("normal").executes($$5 -> CloneCommands.clone((CommandSourceStack)$$5.getSource(), (DimensionAndPosition)$$0.apply($$5), (DimensionAndPosition)$$1.apply($$5), (DimensionAndPosition)$$2.apply($$5), (Predicate)$$3.apply($$5), Mode.NORMAL, $$4)));
    }

    private static int clone(CommandSourceStack $$0, DimensionAndPosition $$1, DimensionAndPosition $$2, DimensionAndPosition $$3, Predicate<BlockInWorld> $$4, Mode $$5, boolean $$6) throws CommandSyntaxException {
        int $$16;
        BlockPos $$7 = $$1.position();
        BlockPos $$8 = $$2.position();
        BoundingBox $$9 = BoundingBox.fromCorners($$7, $$8);
        BlockPos $$10 = $$3.position();
        BlockPos $$11 = $$10.offset($$9.getLength());
        BoundingBox $$12 = BoundingBox.fromCorners($$10, $$11);
        ServerLevel $$13 = $$1.dimension();
        ServerLevel $$14 = $$3.dimension();
        if (!$$5.canOverlap() && $$13 == $$14 && $$12.intersects($$9)) {
            throw ERROR_OVERLAP.create();
        }
        int $$15 = $$9.getXSpan() * $$9.getYSpan() * $$9.getZSpan();
        if ($$15 > ($$16 = $$0.getLevel().getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS).intValue())) {
            throw ERROR_AREA_TOO_LARGE.create((Object)$$16, (Object)$$15);
        }
        if (!$$13.hasChunksAt($$7, $$8) || !$$14.hasChunksAt($$10, $$11)) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
        if ($$14.isDebug()) {
            throw ERROR_FAILED.create();
        }
        ArrayList $$17 = Lists.newArrayList();
        ArrayList $$18 = Lists.newArrayList();
        ArrayList $$19 = Lists.newArrayList();
        LinkedList $$20 = Lists.newLinkedList();
        int $$21 = 0;
        try (ProblemReporter.ScopedCollector $$22 = new ProblemReporter.ScopedCollector(LOGGER);){
            BlockPos $$23 = new BlockPos($$12.minX() - $$9.minX(), $$12.minY() - $$9.minY(), $$12.minZ() - $$9.minZ());
            for (int $$24 = $$9.minZ(); $$24 <= $$9.maxZ(); ++$$24) {
                for (int $$25 = $$9.minY(); $$25 <= $$9.maxY(); ++$$25) {
                    for (int $$26 = $$9.minX(); $$26 <= $$9.maxX(); ++$$26) {
                        BlockPos $$27 = new BlockPos($$26, $$25, $$24);
                        BlockPos $$28 = $$27.offset($$23);
                        BlockInWorld $$29 = new BlockInWorld($$13, $$27, false);
                        BlockState $$30 = $$29.getState();
                        if (!$$4.test($$29)) continue;
                        BlockEntity $$31 = $$13.getBlockEntity($$27);
                        if ($$31 != null) {
                            TagValueOutput $$32 = TagValueOutput.createWithContext($$22.forChild($$31.problemPath()), $$0.registryAccess());
                            $$31.saveCustomOnly($$32);
                            CloneBlockEntityInfo $$33 = new CloneBlockEntityInfo($$32.buildResult(), $$31.components());
                            $$18.add(new CloneBlockInfo($$28, $$30, $$33, $$14.getBlockState($$28)));
                            $$20.addLast($$27);
                            continue;
                        }
                        if ($$30.isSolidRender() || $$30.isCollisionShapeFullBlock($$13, $$27)) {
                            $$17.add(new CloneBlockInfo($$28, $$30, null, $$14.getBlockState($$28)));
                            $$20.addLast($$27);
                            continue;
                        }
                        $$19.add(new CloneBlockInfo($$28, $$30, null, $$14.getBlockState($$28)));
                        $$20.addFirst($$27);
                    }
                }
            }
            int $$34 = 2 | ($$6 ? 816 : 0);
            if ($$5 == Mode.MOVE) {
                for (BlockPos $$35 : $$20) {
                    $$13.setBlock($$35, Blocks.BARRIER.defaultBlockState(), $$34 | 0x330);
                }
                int $$36 = $$6 ? $$34 : 3;
                for (BlockPos $$37 : $$20) {
                    $$13.setBlock($$37, Blocks.AIR.defaultBlockState(), $$36);
                }
            }
            ArrayList $$38 = Lists.newArrayList();
            $$38.addAll($$17);
            $$38.addAll($$18);
            $$38.addAll($$19);
            List $$39 = Lists.reverse((List)$$38);
            for (CloneBlockInfo $$40 : $$39) {
                $$14.setBlock($$40.pos, Blocks.BARRIER.defaultBlockState(), $$34 | 0x330);
            }
            for (CloneBlockInfo $$41 : $$38) {
                if (!$$14.setBlock($$41.pos, $$41.state, $$34)) continue;
                ++$$21;
            }
            for (CloneBlockInfo $$42 : $$18) {
                BlockEntity $$43 = $$14.getBlockEntity($$42.pos);
                if ($$42.blockEntityInfo != null && $$43 != null) {
                    $$43.loadCustomOnly(TagValueInput.create($$22.forChild($$43.problemPath()), (HolderLookup.Provider)$$14.registryAccess(), $$42.blockEntityInfo.tag));
                    $$43.setComponents($$42.blockEntityInfo.components);
                    $$43.setChanged();
                }
                $$14.setBlock($$42.pos, $$42.state, $$34);
            }
            if (!$$6) {
                for (CloneBlockInfo $$44 : $$39) {
                    $$14.updateNeighboursOnBlockSet($$44.pos, $$44.previousStateAtDestination);
                }
            }
            ((LevelTicks)$$14.getBlockTicks()).copyAreaFrom($$13.getBlockTicks(), $$9, $$23);
        }
        if ($$21 == 0) {
            throw ERROR_FAILED.create();
        }
        int $$45 = $$21;
        $$0.sendSuccess(() -> Component.translatable("commands.clone.success", $$45), true);
        return $$21;
    }

    record DimensionAndPosition(ServerLevel dimension, BlockPos position) {
    }

    static enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean canOverlap;

        private Mode(boolean $$0) {
            this.canOverlap = $$0;
        }

        public boolean canOverlap() {
            return this.canOverlap;
        }
    }

    static final class CloneBlockEntityInfo
    extends Record {
        final CompoundTag tag;
        final DataComponentMap components;

        CloneBlockEntityInfo(CompoundTag $$0, DataComponentMap $$1) {
            this.tag = $$0;
            this.components = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CloneBlockEntityInfo.class, "tag;components", "tag", "components"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CloneBlockEntityInfo.class, "tag;components", "tag", "components"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CloneBlockEntityInfo.class, "tag;components", "tag", "components"}, this, $$0);
        }

        public CompoundTag tag() {
            return this.tag;
        }

        public DataComponentMap components() {
            return this.components;
        }
    }

    static final class CloneBlockInfo
    extends Record {
        final BlockPos pos;
        final BlockState state;
        final @Nullable CloneBlockEntityInfo blockEntityInfo;
        final BlockState previousStateAtDestination;

        CloneBlockInfo(BlockPos $$0, BlockState $$1, @Nullable CloneBlockEntityInfo $$2, BlockState $$3) {
            this.pos = $$0;
            this.state = $$1;
            this.blockEntityInfo = $$2;
            this.previousStateAtDestination = $$3;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CloneBlockInfo.class, "pos;state;blockEntityInfo;previousStateAtDestination", "pos", "state", "blockEntityInfo", "previousStateAtDestination"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CloneBlockInfo.class, "pos;state;blockEntityInfo;previousStateAtDestination", "pos", "state", "blockEntityInfo", "previousStateAtDestination"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CloneBlockInfo.class, "pos;state;blockEntityInfo;previousStateAtDestination", "pos", "state", "blockEntityInfo", "previousStateAtDestination"}, this, $$0);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public BlockState state() {
            return this.state;
        }

        public @Nullable CloneBlockEntityInfo blockEntityInfo() {
            return this.blockEntityInfo;
        }

        public BlockState previousStateAtDestination() {
            return this.previousStateAtDestination;
        }
    }
}

