/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.RedirectModifier
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.HeightmapTypeArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.SlotsArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.commands.InCommandFunction;
import net.minecraft.server.commands.ItemCommands;
import net.minecraft.server.commands.StopwatchCommand;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.Stopwatch;
import net.minecraft.world.Stopwatches;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ExecuteCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TEST_AREA = 32768;
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.execute.blocks.toobig", $$0, $$1));
    private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.execute.conditional.fail"));
    private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.execute.conditional.fail_count", $$0));
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType ERROR_FUNCTION_CONDITION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.execute.function.instantiationFailure", $$0, $$1));

    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$1) {
        LiteralCommandNode $$2 = $$02.register((LiteralArgumentBuilder)Commands.literal("execute").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)));
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("execute").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("run").redirect((CommandNode)$$02.getRoot()))).then(ExecuteCommand.addConditionals((CommandNode<CommandSourceStack>)$$2, Commands.literal("if"), true, $$1))).then(ExecuteCommand.addConditionals((CommandNode<CommandSourceStack>)$$2, Commands.literal("unless"), false, $$1))).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork((CommandNode)$$2, $$0 -> {
            ArrayList $$1 = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)$$0, "targets")) {
                $$1.add(((CommandSourceStack)$$0.getSource()).withEntity(entity));
            }
            return $$1;
        })))).then(Commands.literal("at").then(Commands.argument("targets", EntityArgument.entities()).fork((CommandNode)$$2, $$0 -> {
            ArrayList $$1 = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)$$0, "targets")) {
                $$1.add(((CommandSourceStack)$$0.getSource()).withLevel((ServerLevel)entity.level()).withPosition(entity.position()).withRotation(entity.getRotationVector()));
            }
            return $$1;
        })))).then(((LiteralArgumentBuilder)Commands.literal("store").then(ExecuteCommand.wrapStores((LiteralCommandNode<CommandSourceStack>)$$2, Commands.literal("result"), true))).then(ExecuteCommand.wrapStores((LiteralCommandNode<CommandSourceStack>)$$2, Commands.literal("success"), false)))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("positioned").then(Commands.argument("pos", Vec3Argument.vec3()).redirect((CommandNode)$$2, $$0 -> ((CommandSourceStack)$$0.getSource()).withPosition(Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "pos")).withAnchor(EntityAnchorArgument.Anchor.FEET)))).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork((CommandNode)$$2, $$0 -> {
            ArrayList $$1 = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)$$0, "targets")) {
                $$1.add(((CommandSourceStack)$$0.getSource()).withPosition(entity.position()));
            }
            return $$1;
        })))).then(Commands.literal("over").then(Commands.argument("heightmap", HeightmapTypeArgument.heightmap()).redirect((CommandNode)$$2, $$0 -> {
            Vec3 $$1 = ((CommandSourceStack)$$0.getSource()).getPosition();
            ServerLevel $$2 = ((CommandSourceStack)$$0.getSource()).getLevel();
            double $$3 = $$1.x();
            double $$4 = $$1.z();
            if (!$$2.hasChunk(SectionPos.blockToSectionCoord($$3), SectionPos.blockToSectionCoord($$4))) {
                throw BlockPosArgument.ERROR_NOT_LOADED.create();
            }
            int $$5 = $$2.getHeight(HeightmapTypeArgument.getHeightmap((CommandContext<CommandSourceStack>)$$0, "heightmap"), Mth.floor($$3), Mth.floor($$4));
            return ((CommandSourceStack)$$0.getSource()).withPosition(new Vec3($$3, $$5, $$4));
        }))))).then(((LiteralArgumentBuilder)Commands.literal("rotated").then(Commands.argument("rot", RotationArgument.rotation()).redirect((CommandNode)$$2, $$0 -> ((CommandSourceStack)$$0.getSource()).withRotation(RotationArgument.getRotation((CommandContext<CommandSourceStack>)$$0, "rot").getRotation((CommandSourceStack)$$0.getSource()))))).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork((CommandNode)$$2, $$0 -> {
            ArrayList $$1 = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)$$0, "targets")) {
                $$1.add(((CommandSourceStack)$$0.getSource()).withRotation(entity.getRotationVector()));
            }
            return $$1;
        }))))).then(((LiteralArgumentBuilder)Commands.literal("facing").then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("anchor", EntityAnchorArgument.anchor()).fork((CommandNode)$$2, $$0 -> {
            ArrayList $$1 = Lists.newArrayList();
            EntityAnchorArgument.Anchor $$2 = EntityAnchorArgument.getAnchor((CommandContext<CommandSourceStack>)$$0, "anchor");
            for (Entity entity : EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)$$0, "targets")) {
                $$1.add(((CommandSourceStack)$$0.getSource()).facing(entity, $$2));
            }
            return $$1;
        }))))).then(Commands.argument("pos", Vec3Argument.vec3()).redirect((CommandNode)$$2, $$0 -> ((CommandSourceStack)$$0.getSource()).facing(Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "pos")))))).then(Commands.literal("align").then(Commands.argument("axes", SwizzleArgument.swizzle()).redirect((CommandNode)$$2, $$0 -> ((CommandSourceStack)$$0.getSource()).withPosition(((CommandSourceStack)$$0.getSource()).getPosition().align(SwizzleArgument.getSwizzle((CommandContext<CommandSourceStack>)$$0, "axes"))))))).then(Commands.literal("anchored").then(Commands.argument("anchor", EntityAnchorArgument.anchor()).redirect((CommandNode)$$2, $$0 -> ((CommandSourceStack)$$0.getSource()).withAnchor(EntityAnchorArgument.getAnchor((CommandContext<CommandSourceStack>)$$0, "anchor")))))).then(Commands.literal("in").then(Commands.argument("dimension", DimensionArgument.dimension()).redirect((CommandNode)$$2, $$0 -> ((CommandSourceStack)$$0.getSource()).withLevel(DimensionArgument.getDimension((CommandContext<CommandSourceStack>)$$0, "dimension")))))).then(Commands.literal("summon").then(Commands.argument("entity", ResourceArgument.resource($$1, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES)).redirect((CommandNode)$$2, $$0 -> ExecuteCommand.spawnEntityAndRedirect((CommandSourceStack)$$0.getSource(), ResourceArgument.getSummonableEntityType((CommandContext<CommandSourceStack>)$$0, "entity")))))).then(ExecuteCommand.createRelationOperations((CommandNode<CommandSourceStack>)$$2, Commands.literal("on"))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapStores(LiteralCommandNode<CommandSourceStack> $$0, LiteralArgumentBuilder<CommandSourceStack> $$12, boolean $$2) {
        $$12.then(Commands.literal("score").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).redirect($$0, $$1 -> ExecuteCommand.storeValue((CommandSourceStack)$$1.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)$$1, "targets"), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)$$1, "objective"), $$2)))));
        $$12.then(Commands.literal("bossbar").then(((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id()).suggests(BossBarCommands.SUGGEST_BOSS_BAR).then(Commands.literal("value").redirect($$0, $$1 -> ExecuteCommand.storeValue((CommandSourceStack)$$1.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$1), true, $$2)))).then(Commands.literal("max").redirect($$0, $$1 -> ExecuteCommand.storeValue((CommandSourceStack)$$1.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$1), false, $$2)))));
        for (DataCommands.DataProvider $$32 : DataCommands.TARGET_PROVIDERS) {
            $$32.wrap((ArgumentBuilder<CommandSourceStack, ?>)$$12, $$3 -> $$3.then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath()).then(Commands.literal("int").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)$$0, $$2 -> ExecuteCommand.storeData((CommandSourceStack)$$2.getSource(), $$32.access((CommandContext<CommandSourceStack>)$$2), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$2, "path"), $$1 -> IntTag.valueOf((int)((double)$$1 * DoubleArgumentType.getDouble((CommandContext)$$2, (String)"scale"))), $$2))))).then(Commands.literal("float").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)$$0, $$2 -> ExecuteCommand.storeData((CommandSourceStack)$$2.getSource(), $$32.access((CommandContext<CommandSourceStack>)$$2), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$2, "path"), $$1 -> FloatTag.valueOf((float)((double)$$1 * DoubleArgumentType.getDouble((CommandContext)$$2, (String)"scale"))), $$2))))).then(Commands.literal("short").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)$$0, $$2 -> ExecuteCommand.storeData((CommandSourceStack)$$2.getSource(), $$32.access((CommandContext<CommandSourceStack>)$$2), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$2, "path"), $$1 -> ShortTag.valueOf((short)((double)$$1 * DoubleArgumentType.getDouble((CommandContext)$$2, (String)"scale"))), $$2))))).then(Commands.literal("long").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)$$0, $$2 -> ExecuteCommand.storeData((CommandSourceStack)$$2.getSource(), $$32.access((CommandContext<CommandSourceStack>)$$2), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$2, "path"), $$1 -> LongTag.valueOf((long)((double)$$1 * DoubleArgumentType.getDouble((CommandContext)$$2, (String)"scale"))), $$2))))).then(Commands.literal("double").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)$$0, $$2 -> ExecuteCommand.storeData((CommandSourceStack)$$2.getSource(), $$32.access((CommandContext<CommandSourceStack>)$$2), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$2, "path"), $$1 -> DoubleTag.valueOf((double)$$1 * DoubleArgumentType.getDouble((CommandContext)$$2, (String)"scale")), $$2))))).then(Commands.literal("byte").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)$$0, $$2 -> ExecuteCommand.storeData((CommandSourceStack)$$2.getSource(), $$32.access((CommandContext<CommandSourceStack>)$$2), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$2, "path"), $$1 -> ByteTag.valueOf((byte)((double)$$1 * DoubleArgumentType.getDouble((CommandContext)$$2, (String)"scale"))), $$2))))));
        }
        return $$12;
    }

    private static CommandSourceStack storeValue(CommandSourceStack $$0, Collection<ScoreHolder> $$1, Objective $$2, boolean $$3) {
        ServerScoreboard $$42 = $$0.getServer().getScoreboard();
        return $$0.withCallback(($$4, $$5) -> {
            for (ScoreHolder $$6 : $$1) {
                ScoreAccess $$7 = $$42.getOrCreatePlayerScore($$6, $$2);
                int $$8 = $$3 ? $$5 : ($$4 ? 1 : 0);
                $$7.set($$8);
            }
        }, CommandResultCallback::chain);
    }

    private static CommandSourceStack storeValue(CommandSourceStack $$0, CustomBossEvent $$1, boolean $$2, boolean $$32) {
        return $$0.withCallback(($$3, $$4) -> {
            int $$5;
            int n = $$32 ? $$4 : ($$5 = $$3 ? 1 : 0);
            if ($$2) {
                $$1.setValue($$5);
            } else {
                $$1.setMax($$5);
            }
        }, CommandResultCallback::chain);
    }

    private static CommandSourceStack storeData(CommandSourceStack $$0, DataAccessor $$1, NbtPathArgument.NbtPath $$2, IntFunction<Tag> $$3, boolean $$42) {
        return $$0.withCallback(($$4, $$5) -> {
            try {
                CompoundTag $$6 = $$1.getData();
                int $$7 = $$42 ? $$5 : ($$4 ? 1 : 0);
                $$2.set($$6, (Tag)$$3.apply($$7));
                $$1.setData($$6);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }, CommandResultCallback::chain);
    }

    private static boolean isChunkLoaded(ServerLevel $$0, BlockPos $$1) {
        ChunkPos $$2 = new ChunkPos($$1);
        LevelChunk $$3 = $$0.getChunkSource().getChunkNow($$2.x, $$2.z);
        if ($$3 != null) {
            return $$3.getFullStatus() == FullChunkStatus.ENTITY_TICKING && $$0.areEntitiesLoaded($$2.toLong());
        }
        return false;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditionals(CommandNode<CommandSourceStack> $$03, LiteralArgumentBuilder<CommandSourceStack> $$12, boolean $$2, CommandBuildContext $$32) {
        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)$$12.then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(ExecuteCommand.addConditional($$03, Commands.argument("block", BlockPredicateArgument.blockPredicate($$32)), $$2, $$0 -> BlockPredicateArgument.getBlockPredicate((CommandContext<CommandSourceStack>)$$0, "block").test(new BlockInWorld(((CommandSourceStack)$$0.getSource()).getLevel(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), true))))))).then(Commands.literal("biome").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(ExecuteCommand.addConditional($$03, Commands.argument("biome", ResourceOrTagArgument.resourceOrTag($$32, Registries.BIOME)), $$2, $$0 -> ResourceOrTagArgument.getResourceOrTag((CommandContext<CommandSourceStack>)$$0, "biome", Registries.BIOME).test(((CommandSourceStack)$$0.getSource()).getLevel().getBiome(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos")))))))).then(Commands.literal("loaded").then(ExecuteCommand.addConditional($$03, Commands.argument("pos", BlockPosArgument.blockPos()), $$2, $$0 -> ExecuteCommand.isChunkLoaded(((CommandSourceStack)$$0.getSource()).getLevel(), BlockPosArgument.getBlockPos((CommandContext<CommandSourceStack>)$$0, "pos")))))).then(Commands.literal("dimension").then(ExecuteCommand.addConditional($$03, Commands.argument("dimension", DimensionArgument.dimension()), $$2, $$0 -> DimensionArgument.getDimension((CommandContext<CommandSourceStack>)$$0, "dimension") == ((CommandSourceStack)$$0.getSource()).getLevel())))).then(Commands.literal("score").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targetObjective", ObjectiveArgument.objective()).then(Commands.literal("=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional($$03, Commands.argument("sourceObjective", ObjectiveArgument.objective()), $$2, $$02 -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)$$02, (int $$0, int $$1) -> $$0 == $$1)))))).then(Commands.literal("<").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional($$03, Commands.argument("sourceObjective", ObjectiveArgument.objective()), $$2, $$02 -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)$$02, (int $$0, int $$1) -> $$0 < $$1)))))).then(Commands.literal("<=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional($$03, Commands.argument("sourceObjective", ObjectiveArgument.objective()), $$2, $$02 -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)$$02, (int $$0, int $$1) -> $$0 <= $$1)))))).then(Commands.literal(">").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional($$03, Commands.argument("sourceObjective", ObjectiveArgument.objective()), $$2, $$02 -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)$$02, (int $$0, int $$1) -> $$0 > $$1)))))).then(Commands.literal(">=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional($$03, Commands.argument("sourceObjective", ObjectiveArgument.objective()), $$2, $$02 -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)$$02, (int $$0, int $$1) -> $$0 >= $$1)))))).then(Commands.literal("matches").then(ExecuteCommand.addConditional($$03, Commands.argument("range", RangeArgument.intRange()), $$2, $$0 -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)$$0, RangeArgument.Ints.getRange((CommandContext<CommandSourceStack>)$$0, "range"))))))))).then(Commands.literal("blocks").then(Commands.argument("start", BlockPosArgument.blockPos()).then(Commands.argument("end", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("destination", BlockPosArgument.blockPos()).then(ExecuteCommand.addIfBlocksConditional($$03, Commands.literal("all"), $$2, false))).then(ExecuteCommand.addIfBlocksConditional($$03, Commands.literal("masked"), $$2, true))))))).then(Commands.literal("entity").then(((RequiredArgumentBuilder)Commands.argument("entities", EntityArgument.entities()).fork($$03, $$1 -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)$$1, $$2, !EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)$$1, "entities").isEmpty()))).executes(ExecuteCommand.createNumericConditionalHandler($$2, $$0 -> EntityArgument.getOptionalEntities((CommandContext<CommandSourceStack>)$$0, "entities").size()))))).then(Commands.literal("predicate").then(ExecuteCommand.addConditional($$03, Commands.argument("predicate", ResourceOrIdArgument.lootPredicate($$32)), $$2, $$0 -> ExecuteCommand.checkCustomPredicate((CommandSourceStack)$$0.getSource(), ResourceOrIdArgument.getLootPredicate((CommandContext<CommandSourceStack>)$$0, "predicate")))))).then(Commands.literal("function").then(Commands.argument("name", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION).fork($$03, (RedirectModifier)new ExecuteIfFunctionCustomModifier($$2))))).then(((LiteralArgumentBuilder)Commands.literal("items").then(Commands.literal("entity").then(Commands.argument("entities", EntityArgument.entities()).then(Commands.argument("slots", SlotsArgument.slots()).then(((RequiredArgumentBuilder)Commands.argument("item_predicate", ItemPredicateArgument.itemPredicate($$32)).fork($$03, $$1 -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)$$1, $$2, ExecuteCommand.countItems(EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$1, "entities"), SlotsArgument.getSlots((CommandContext<CommandSourceStack>)$$1, "slots"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)$$1, "item_predicate")) > 0))).executes(ExecuteCommand.createNumericConditionalHandler($$2, $$0 -> ExecuteCommand.countItems(EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "entities"), SlotsArgument.getSlots((CommandContext<CommandSourceStack>)$$0, "slots"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)$$0, "item_predicate"))))))))).then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("slots", SlotsArgument.slots()).then(((RequiredArgumentBuilder)Commands.argument("item_predicate", ItemPredicateArgument.itemPredicate($$32)).fork($$03, $$1 -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)$$1, $$2, ExecuteCommand.countItems((CommandSourceStack)$$1.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$1, "pos"), SlotsArgument.getSlots((CommandContext<CommandSourceStack>)$$1, "slots"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)$$1, "item_predicate")) > 0))).executes(ExecuteCommand.createNumericConditionalHandler($$2, $$0 -> ExecuteCommand.countItems((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), SlotsArgument.getSlots((CommandContext<CommandSourceStack>)$$0, "slots"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)$$0, "item_predicate")))))))))).then(Commands.literal("stopwatch").then(Commands.argument("id", IdentifierArgument.id()).suggests(StopwatchCommand.SUGGEST_STOPWATCHES).then(ExecuteCommand.addConditional($$03, Commands.argument("range", RangeArgument.floatRange()), $$2, $$0 -> ExecuteCommand.checkStopwatch((CommandContext<CommandSourceStack>)$$0, RangeArgument.Floats.getRange((CommandContext<CommandSourceStack>)$$0, "range"))))));
        for (DataCommands.DataProvider $$4 : DataCommands.SOURCE_PROVIDERS) {
            $$12.then($$4.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("data"), $$3 -> $$3.then(((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath()).fork($$03, $$2 -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)$$2, $$2, ExecuteCommand.checkMatchingData($$4.access((CommandContext<CommandSourceStack>)$$2), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$2, "path")) > 0))).executes(ExecuteCommand.createNumericConditionalHandler($$2, $$1 -> ExecuteCommand.checkMatchingData($$4.access((CommandContext<CommandSourceStack>)$$1), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$1, "path")))))));
        }
        return $$12;
    }

    private static int countItems(Iterable<? extends SlotProvider> $$0, SlotRange $$1, Predicate<ItemStack> $$2) {
        int $$3 = 0;
        for (SlotProvider slotProvider : $$0) {
            IntList $$5 = $$1.slots();
            for (int $$6 = 0; $$6 < $$5.size(); ++$$6) {
                ItemStack $$9;
                int $$7 = $$5.getInt($$6);
                SlotAccess $$8 = slotProvider.getSlot($$7);
                if ($$8 == null || !$$2.test($$9 = $$8.get())) continue;
                $$3 += $$9.getCount();
            }
        }
        return $$3;
    }

    private static int countItems(CommandSourceStack $$0, BlockPos $$1, SlotRange $$2, Predicate<ItemStack> $$3) throws CommandSyntaxException {
        int $$4 = 0;
        Container $$5 = ItemCommands.getContainer($$0, $$1, ItemCommands.ERROR_SOURCE_NOT_A_CONTAINER);
        int $$6 = $$5.getContainerSize();
        IntList $$7 = $$2.slots();
        for (int $$8 = 0; $$8 < $$7.size(); ++$$8) {
            ItemStack $$10;
            int $$9 = $$7.getInt($$8);
            if ($$9 < 0 || $$9 >= $$6 || !$$3.test($$10 = $$5.getItem($$9))) continue;
            $$4 += $$10.getCount();
        }
        return $$4;
    }

    private static Command<CommandSourceStack> createNumericConditionalHandler(boolean $$0, CommandNumericPredicate $$12) {
        if ($$0) {
            return $$1 -> {
                int $$2 = $$12.test((CommandContext<CommandSourceStack>)$$1);
                if ($$2 > 0) {
                    ((CommandSourceStack)$$1.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", $$2), false);
                    return $$2;
                }
                throw ERROR_CONDITIONAL_FAILED.create();
            };
        }
        return $$1 -> {
            int $$2 = $$12.test((CommandContext<CommandSourceStack>)$$1);
            if ($$2 == 0) {
                ((CommandSourceStack)$$1.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw ERROR_CONDITIONAL_FAILED_COUNT.create((Object)$$2);
        };
    }

    private static int checkMatchingData(DataAccessor $$0, NbtPathArgument.NbtPath $$1) throws CommandSyntaxException {
        return $$1.countMatching($$0.getData());
    }

    private static boolean checkScore(CommandContext<CommandSourceStack> $$0, IntBiPredicate $$1) throws CommandSyntaxException {
        ScoreHolder $$2 = ScoreHolderArgument.getName($$0, "target");
        Objective $$3 = ObjectiveArgument.getObjective($$0, "targetObjective");
        ScoreHolder $$4 = ScoreHolderArgument.getName($$0, "source");
        Objective $$5 = ObjectiveArgument.getObjective($$0, "sourceObjective");
        ServerScoreboard $$6 = ((CommandSourceStack)$$0.getSource()).getServer().getScoreboard();
        ReadOnlyScoreInfo $$7 = $$6.getPlayerScoreInfo($$2, $$3);
        ReadOnlyScoreInfo $$8 = $$6.getPlayerScoreInfo($$4, $$5);
        if ($$7 == null || $$8 == null) {
            return false;
        }
        return $$1.test($$7.value(), $$8.value());
    }

    private static boolean checkScore(CommandContext<CommandSourceStack> $$0, MinMaxBounds.Ints $$1) throws CommandSyntaxException {
        ScoreHolder $$2 = ScoreHolderArgument.getName($$0, "target");
        Objective $$3 = ObjectiveArgument.getObjective($$0, "targetObjective");
        ServerScoreboard $$4 = ((CommandSourceStack)$$0.getSource()).getServer().getScoreboard();
        ReadOnlyScoreInfo $$5 = $$4.getPlayerScoreInfo($$2, $$3);
        if ($$5 == null) {
            return false;
        }
        return $$1.matches($$5.value());
    }

    private static boolean checkStopwatch(CommandContext<CommandSourceStack> $$0, MinMaxBounds.Doubles $$1) throws CommandSyntaxException {
        Identifier $$2 = IdentifierArgument.getId($$0, "id");
        Stopwatches $$3 = ((CommandSourceStack)$$0.getSource()).getServer().getStopwatches();
        Stopwatch $$4 = $$3.get($$2);
        if ($$4 == null) {
            throw StopwatchCommand.ERROR_DOES_NOT_EXIST.create((Object)$$2);
        }
        long $$5 = Stopwatches.currentTime();
        double $$6 = $$4.elapsedSeconds($$5);
        return $$1.matches($$6);
    }

    private static boolean checkCustomPredicate(CommandSourceStack $$0, Holder<LootItemCondition> $$1) {
        ServerLevel $$2 = $$0.getLevel();
        LootParams $$3 = new LootParams.Builder($$2).withParameter(LootContextParams.ORIGIN, $$0.getPosition()).withOptionalParameter(LootContextParams.THIS_ENTITY, $$0.getEntity()).create(LootContextParamSets.COMMAND);
        LootContext $$4 = new LootContext.Builder($$3).create(Optional.empty());
        $$4.pushVisitedElement(LootContext.createVisitedEntry($$1.value()));
        return $$1.value().test($$4);
    }

    private static Collection<CommandSourceStack> expect(CommandContext<CommandSourceStack> $$0, boolean $$1, boolean $$2) {
        if ($$2 == $$1) {
            return Collections.singleton((CommandSourceStack)$$0.getSource());
        }
        return Collections.emptyList();
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditional(CommandNode<CommandSourceStack> $$0, ArgumentBuilder<CommandSourceStack, ?> $$1, boolean $$22, CommandPredicate $$3) {
        return $$1.fork($$0, $$2 -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)$$2, $$22, $$3.test((CommandContext<CommandSourceStack>)$$2))).executes($$2 -> {
            if ($$22 == $$3.test((CommandContext<CommandSourceStack>)$$2)) {
                ((CommandSourceStack)$$2.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw ERROR_CONDITIONAL_FAILED.create();
        });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addIfBlocksConditional(CommandNode<CommandSourceStack> $$0, ArgumentBuilder<CommandSourceStack, ?> $$12, boolean $$22, boolean $$3) {
        return $$12.fork($$0, $$2 -> ExecuteCommand.expect((CommandContext<CommandSourceStack>)$$2, $$22, ExecuteCommand.checkRegions((CommandContext<CommandSourceStack>)$$2, $$3).isPresent())).executes($$22 ? $$1 -> ExecuteCommand.checkIfRegions((CommandContext<CommandSourceStack>)$$1, $$3) : $$1 -> ExecuteCommand.checkUnlessRegions((CommandContext<CommandSourceStack>)$$1, $$3));
    }

    private static int checkIfRegions(CommandContext<CommandSourceStack> $$0, boolean $$1) throws CommandSyntaxException {
        OptionalInt $$2 = ExecuteCommand.checkRegions($$0, $$1);
        if ($$2.isPresent()) {
            ((CommandSourceStack)$$0.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", $$2.getAsInt()), false);
            return $$2.getAsInt();
        }
        throw ERROR_CONDITIONAL_FAILED.create();
    }

    private static int checkUnlessRegions(CommandContext<CommandSourceStack> $$0, boolean $$1) throws CommandSyntaxException {
        OptionalInt $$2 = ExecuteCommand.checkRegions($$0, $$1);
        if ($$2.isPresent()) {
            throw ERROR_CONDITIONAL_FAILED_COUNT.create((Object)$$2.getAsInt());
        }
        ((CommandSourceStack)$$0.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
        return 1;
    }

    private static OptionalInt checkRegions(CommandContext<CommandSourceStack> $$0, boolean $$1) throws CommandSyntaxException {
        return ExecuteCommand.checkRegions(((CommandSourceStack)$$0.getSource()).getLevel(), BlockPosArgument.getLoadedBlockPos($$0, "start"), BlockPosArgument.getLoadedBlockPos($$0, "end"), BlockPosArgument.getLoadedBlockPos($$0, "destination"), $$1);
    }

    private static OptionalInt checkRegions(ServerLevel $$0, BlockPos $$1, BlockPos $$2, BlockPos $$3, boolean $$4) throws CommandSyntaxException {
        BoundingBox $$5 = BoundingBox.fromCorners($$1, $$2);
        BoundingBox $$6 = BoundingBox.fromCorners($$3, $$3.offset($$5.getLength()));
        BlockPos $$7 = new BlockPos($$6.minX() - $$5.minX(), $$6.minY() - $$5.minY(), $$6.minZ() - $$5.minZ());
        int $$8 = $$5.getXSpan() * $$5.getYSpan() * $$5.getZSpan();
        if ($$8 > 32768) {
            throw ERROR_AREA_TOO_LARGE.create((Object)32768, (Object)$$8);
        }
        int $$9 = 0;
        RegistryAccess $$10 = $$0.registryAccess();
        try (ProblemReporter.ScopedCollector $$11 = new ProblemReporter.ScopedCollector(LOGGER);){
            for (int $$12 = $$5.minZ(); $$12 <= $$5.maxZ(); ++$$12) {
                for (int $$13 = $$5.minY(); $$13 <= $$5.maxY(); ++$$13) {
                    for (int $$14 = $$5.minX(); $$14 <= $$5.maxX(); ++$$14) {
                        BlockPos $$15 = new BlockPos($$14, $$13, $$12);
                        BlockPos $$16 = $$15.offset($$7);
                        BlockState $$17 = $$0.getBlockState($$15);
                        if ($$4 && $$17.is(Blocks.AIR)) continue;
                        if ($$17 != $$0.getBlockState($$16)) {
                            OptionalInt optionalInt = OptionalInt.empty();
                            return optionalInt;
                        }
                        BlockEntity $$18 = $$0.getBlockEntity($$15);
                        BlockEntity $$19 = $$0.getBlockEntity($$16);
                        if ($$18 != null) {
                            OptionalInt optionalInt;
                            if ($$19 == null) {
                                optionalInt = OptionalInt.empty();
                                return optionalInt;
                            }
                            if ($$19.getType() != $$18.getType()) {
                                optionalInt = OptionalInt.empty();
                                return optionalInt;
                            }
                            if (!$$18.components().equals($$19.components())) {
                                optionalInt = OptionalInt.empty();
                                return optionalInt;
                            }
                            TagValueOutput $$20 = TagValueOutput.createWithContext($$11.forChild($$18.problemPath()), $$10);
                            $$18.saveCustomOnly($$20);
                            CompoundTag $$21 = $$20.buildResult();
                            TagValueOutput $$22 = TagValueOutput.createWithContext($$11.forChild($$19.problemPath()), $$10);
                            $$19.saveCustomOnly($$22);
                            CompoundTag $$23 = $$22.buildResult();
                            if (!$$21.equals($$23)) {
                                OptionalInt optionalInt2 = OptionalInt.empty();
                                return optionalInt2;
                            }
                        }
                        ++$$9;
                    }
                }
            }
        }
        return OptionalInt.of($$9);
    }

    private static RedirectModifier<CommandSourceStack> expandOneToOneEntityRelation(Function<Entity, Optional<Entity>> $$0) {
        return $$12 -> {
            CommandSourceStack $$2 = (CommandSourceStack)$$12.getSource();
            Entity $$3 = $$2.getEntity();
            if ($$3 == null) {
                return List.of();
            }
            return ((Optional)$$0.apply($$3)).filter($$0 -> !$$0.isRemoved()).map($$1 -> List.of($$2.withEntity((Entity)$$1))).orElse(List.of());
        };
    }

    private static RedirectModifier<CommandSourceStack> expandOneToManyEntityRelation(Function<Entity, Stream<Entity>> $$0) {
        return $$1 -> {
            CommandSourceStack $$2 = (CommandSourceStack)$$1.getSource();
            Entity $$3 = $$2.getEntity();
            if ($$3 == null) {
                return List.of();
            }
            return ((Stream)$$0.apply($$3)).filter($$0 -> !$$0.isRemoved()).map($$2::withEntity).toList();
        };
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRelationOperations(CommandNode<CommandSourceStack> $$02, LiteralArgumentBuilder<CommandSourceStack> $$1) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)$$1.then(Commands.literal("owner").fork($$02, ExecuteCommand.expandOneToOneEntityRelation($$0 -> {
            Optional<Object> optional;
            if ($$0 instanceof OwnableEntity) {
                OwnableEntity $$1 = (OwnableEntity)((Object)$$0);
                optional = Optional.ofNullable($$1.getOwner());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(Commands.literal("leasher").fork($$02, ExecuteCommand.expandOneToOneEntityRelation($$0 -> {
            Optional<Object> optional;
            if ($$0 instanceof Leashable) {
                Leashable $$1 = (Leashable)((Object)$$0);
                optional = Optional.ofNullable($$1.getLeashHolder());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(Commands.literal("target").fork($$02, ExecuteCommand.expandOneToOneEntityRelation($$0 -> {
            Optional<Object> optional;
            if ($$0 instanceof Targeting) {
                Targeting $$1 = (Targeting)((Object)$$0);
                optional = Optional.ofNullable($$1.getTarget());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(Commands.literal("attacker").fork($$02, ExecuteCommand.expandOneToOneEntityRelation($$0 -> {
            Optional<Object> optional;
            if ($$0 instanceof Attackable) {
                Attackable $$1 = (Attackable)((Object)$$0);
                optional = Optional.ofNullable($$1.getLastAttacker());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(Commands.literal("vehicle").fork($$02, ExecuteCommand.expandOneToOneEntityRelation($$0 -> Optional.ofNullable($$0.getVehicle()))))).then(Commands.literal("controller").fork($$02, ExecuteCommand.expandOneToOneEntityRelation($$0 -> Optional.ofNullable($$0.getControllingPassenger()))))).then(Commands.literal("origin").fork($$02, ExecuteCommand.expandOneToOneEntityRelation($$0 -> {
            Optional<Object> optional;
            if ($$0 instanceof TraceableEntity) {
                TraceableEntity $$1 = (TraceableEntity)((Object)$$0);
                optional = Optional.ofNullable($$1.getOwner());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(Commands.literal("passengers").fork($$02, ExecuteCommand.expandOneToManyEntityRelation($$0 -> $$0.getPassengers().stream())));
    }

    private static CommandSourceStack spawnEntityAndRedirect(CommandSourceStack $$0, Holder.Reference<EntityType<?>> $$1) throws CommandSyntaxException {
        Entity $$2 = SummonCommand.createEntity($$0, $$1, $$0.getPosition(), new CompoundTag(), true);
        return $$0.withEntity($$2);
    }

    /*
     * Exception decompiling
     */
    public static <T extends ExecutionCommandSource<T>> void scheduleFunctionConditionsAndTest(T $$0, List<T> $$1, Function<T, T> $$2, IntPredicate $$3, ContextChain<T> $$4, @Nullable CompoundTag $$5, ExecutionControl<T> $$6, InCommandFunction<CommandContext<T>, Collection<CommandFunction<T>>> $$7, ChainModifiers $$8) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private static /* synthetic */ void lambda$scheduleFunctionConditionsAndTest$87(List $$0, ExecutionCommandSource $$1, ExecutionControl $$2) {
        for (InstantiatedFunction $$3 : $$0) {
            $$2.queueNext(new CallFunction<ExecutionCommandSource>($$3, $$2.currentFrame().returnValueConsumer(), true).bind($$1));
        }
        $$2.queueNext(FallthroughTask.instance());
    }

    private static /* synthetic */ void lambda$scheduleFunctionConditionsAndTest$86(IntPredicate $$0, List $$1, ExecutionCommandSource $$2, boolean $$3, int $$4) {
        if ($$0.test($$4)) {
            $$1.add($$2);
        }
    }

    @FunctionalInterface
    static interface CommandPredicate {
        public boolean test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface CommandNumericPredicate {
        public int test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }

    static class ExecuteIfFunctionCustomModifier
    implements CustomModifierExecutor.ModifierAdapter<CommandSourceStack> {
        private final IntPredicate check;

        ExecuteIfFunctionCustomModifier(boolean $$02) {
            this.check = $$02 ? $$0 -> $$0 != 0 : $$0 -> $$0 == 0;
        }

        @Override
        public void apply(CommandSourceStack $$02, List<CommandSourceStack> $$1, ContextChain<CommandSourceStack> $$2, ChainModifiers $$3, ExecutionControl<CommandSourceStack> $$4) {
            ExecuteCommand.scheduleFunctionConditionsAndTest($$02, $$1, FunctionCommand::modifySenderForExecution, this.check, $$2, null, $$4, $$0 -> FunctionArgument.getFunctions((CommandContext<CommandSourceStack>)$$0, "name"), $$3);
        }
    }

    @FunctionalInterface
    static interface IntBiPredicate {
        public boolean test(int var1, int var2);
    }
}

