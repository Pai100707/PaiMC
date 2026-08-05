package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
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
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.commands.arguments.RangeArgument.Floats;
import net.minecraft.commands.arguments.RangeArgument.Ints;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.CustomModifierExecutor.ModifierAdapter;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.execution.tasks.IsolatedCall;
import net.minecraft.commands.execution.tasks.BuildContexts.Continuation;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Holder.Reference;
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
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter.ScopedCollector;
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
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.slf4j.Logger;

public class ExecuteCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_TEST_AREA = 32768;
   private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.execute.blocks.toobig", new Object[]{$$0, $$1})
   );
   private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(
      Component.translatable("commands.execute.conditional.fail")
   );
   private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.execute.conditional.fail_count", new Object[]{$$0})
   );
   @VisibleForTesting
   public static final Dynamic2CommandExceptionType ERROR_FUNCTION_CONDITION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.execute.function.instantiationFailure", new Object[]{$$0, $$1})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      LiteralCommandNode<CommandSourceStack> $$2 = $$0.register(
         (LiteralArgumentBuilder)Commands.literal("execute").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
      );
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                         "execute"
                                                      )
                                                      .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                                                   .then(Commands.literal("run").redirect($$0.getRoot())))
                                                .then(addConditionals($$2, Commands.literal("if"), true, $$1)))
                                             .then(addConditionals($$2, Commands.literal("unless"), false, $$1)))
                                          .then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork($$2, $$0x -> {
                                             List<CommandSourceStack> $$1x = Lists.newArrayList();

                                             for (Entity $$2x : EntityArgument.getOptionalEntities($$0x, "targets")) {
                                                $$1x.add(((CommandSourceStack)$$0x.getSource()).withEntity($$2x));
                                             }

                                             return $$1x;
                                          }))))
                                       .then(
                                          Commands.literal("at")
                                             .then(
                                                Commands.argument("targets", EntityArgument.entities())
                                                   .fork(
                                                      $$2,
                                                      $$0x -> {
                                                         List<CommandSourceStack> $$1x = Lists.newArrayList();

                                                         for (Entity $$2x : EntityArgument.getOptionalEntities($$0x, "targets")) {
                                                            $$1x.add(
                                                               ((CommandSourceStack)$$0x.getSource())
                                                                  .withLevel((ServerLevel)$$2x.level())
                                                                  .withPosition($$2x.position())
                                                                  .withRotation($$2x.getRotationVector())
                                                            );
                                                         }

                                                         return $$1x;
                                                      }
                                                   )
                                             )
                                       ))
                                    .then(
                                       ((LiteralArgumentBuilder)Commands.literal("store").then(wrapStores($$2, Commands.literal("result"), true)))
                                          .then(wrapStores($$2, Commands.literal("success"), false))
                                    ))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("positioned")
                                             .then(
                                                Commands.argument("pos", Vec3Argument.vec3())
                                                   .redirect(
                                                      $$2,
                                                      $$0x -> ((CommandSourceStack)$$0x.getSource())
                                                         .withPosition(Vec3Argument.getVec3($$0x, "pos"))
                                                         .withAnchor(Anchor.FEET)
                                                   )
                                             ))
                                          .then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork($$2, $$0x -> {
                                             List<CommandSourceStack> $$1x = Lists.newArrayList();

                                             for (Entity $$2x : EntityArgument.getOptionalEntities($$0x, "targets")) {
                                                $$1x.add(((CommandSourceStack)$$0x.getSource()).withPosition($$2x.position()));
                                             }

                                             return $$1x;
                                          }))))
                                       .then(
                                          Commands.literal("over")
                                             .then(Commands.argument("heightmap", HeightmapTypeArgument.heightmap()).redirect($$2, $$0x -> {
                                                Vec3 $$1x = ((CommandSourceStack)$$0x.getSource()).getPosition();
                                                ServerLevel $$2x = ((CommandSourceStack)$$0x.getSource()).getLevel();
                                                double $$3 = $$1x.x();
                                                double $$4 = $$1x.z();
                                                if (!$$2x.hasChunk(SectionPos.blockToSectionCoord($$3), SectionPos.blockToSectionCoord($$4))) {
                                                   throw BlockPosArgument.ERROR_NOT_LOADED.create();
                                                } else {
                                                   int $$5 = $$2x.getHeight(
                                                      HeightmapTypeArgument.getHeightmap($$0x, "heightmap"), Mth.floor($$3), Mth.floor($$4)
                                                   );
                                                   return ((CommandSourceStack)$$0x.getSource()).withPosition(new Vec3($$3, $$5, $$4));
                                                }
                                             }))
                                       )
                                 ))
                              .then(
                                 ((LiteralArgumentBuilder)Commands.literal("rotated")
                                       .then(
                                          Commands.argument("rot", RotationArgument.rotation())
                                             .redirect(
                                                $$2,
                                                $$0x -> ((CommandSourceStack)$$0x.getSource())
                                                   .withRotation(RotationArgument.getRotation($$0x, "rot").getRotation((CommandSourceStack)$$0x.getSource()))
                                             )
                                       ))
                                    .then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork($$2, $$0x -> {
                                       List<CommandSourceStack> $$1x = Lists.newArrayList();

                                       for (Entity $$2x : EntityArgument.getOptionalEntities($$0x, "targets")) {
                                          $$1x.add(((CommandSourceStack)$$0x.getSource()).withRotation($$2x.getRotationVector()));
                                       }

                                       return $$1x;
                                    })))
                              ))
                           .then(
                              ((LiteralArgumentBuilder)Commands.literal("facing")
                                    .then(
                                       Commands.literal("entity")
                                          .then(
                                             Commands.argument("targets", EntityArgument.entities())
                                                .then(Commands.argument("anchor", EntityAnchorArgument.anchor()).fork($$2, $$0x -> {
                                                   List<CommandSourceStack> $$1x = Lists.newArrayList();
                                                   Anchor $$2x = EntityAnchorArgument.getAnchor($$0x, "anchor");

                                                   for (Entity $$3 : EntityArgument.getOptionalEntities($$0x, "targets")) {
                                                      $$1x.add(((CommandSourceStack)$$0x.getSource()).facing($$3, $$2x));
                                                   }

                                                   return $$1x;
                                                }))
                                          )
                                    ))
                                 .then(
                                    Commands.argument("pos", Vec3Argument.vec3())
                                       .redirect($$2, $$0x -> ((CommandSourceStack)$$0x.getSource()).facing(Vec3Argument.getVec3($$0x, "pos")))
                                 )
                           ))
                        .then(
                           Commands.literal("align")
                              .then(
                                 Commands.argument("axes", SwizzleArgument.swizzle())
                                    .redirect(
                                       $$2,
                                       $$0x -> ((CommandSourceStack)$$0x.getSource())
                                          .withPosition(((CommandSourceStack)$$0x.getSource()).getPosition().align(SwizzleArgument.getSwizzle($$0x, "axes")))
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("anchored")
                           .then(
                              Commands.argument("anchor", EntityAnchorArgument.anchor())
                                 .redirect($$2, $$0x -> ((CommandSourceStack)$$0x.getSource()).withAnchor(EntityAnchorArgument.getAnchor($$0x, "anchor")))
                           )
                     ))
                  .then(
                     Commands.literal("in")
                        .then(
                           Commands.argument("dimension", DimensionArgument.dimension())
                              .redirect($$2, $$0x -> ((CommandSourceStack)$$0x.getSource()).withLevel(DimensionArgument.getDimension($$0x, "dimension")))
                        )
                  ))
               .then(
                  Commands.literal("summon")
                     .then(
                        Commands.argument("entity", ResourceArgument.resource($$1, Registries.ENTITY_TYPE))
                           .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
                           .redirect(
                              $$2,
                              $$0x -> spawnEntityAndRedirect((CommandSourceStack)$$0x.getSource(), ResourceArgument.getSummonableEntityType($$0x, "entity"))
                           )
                     )
               ))
            .then(createRelationOperations($$2, Commands.literal("on")))
      );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> wrapStores(
      LiteralCommandNode<CommandSourceStack> $$0, LiteralArgumentBuilder<CommandSourceStack> $$1, boolean $$2
   ) {
      $$1.then(
         Commands.literal("score")
            .then(
               Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                  .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                  .then(
                     Commands.argument("objective", ObjectiveArgument.objective())
                        .redirect(
                           $$0,
                           $$1x -> storeValue(
                              (CommandSourceStack)$$1x.getSource(),
                              ScoreHolderArgument.getNamesWithDefaultWildcard($$1x, "targets"),
                              ObjectiveArgument.getObjective($$1x, "objective"),
                              $$2
                           )
                        )
                  )
            )
      );
      $$1.then(
         Commands.literal("bossbar")
            .then(
               ((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id())
                     .suggests(BossBarCommands.SUGGEST_BOSS_BAR)
                     .then(
                        Commands.literal("value")
                           .redirect($$0, $$1x -> storeValue((CommandSourceStack)$$1x.getSource(), BossBarCommands.getBossBar($$1x), true, $$2))
                     ))
                  .then(
                     Commands.literal("max")
                        .redirect($$0, $$1x -> storeValue((CommandSourceStack)$$1x.getSource(), BossBarCommands.getBossBar($$1x), false, $$2))
                  )
            )
      );

      for (DataCommands.DataProvider $$3 : DataCommands.TARGET_PROVIDERS) {
         $$3.wrap(
            $$1,
            $$3x -> $$3x.then(
               ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                    "path", NbtPathArgument.nbtPath()
                                 )
                                 .then(
                                    Commands.literal("int")
                                       .then(
                                          Commands.argument("scale", DoubleArgumentType.doubleArg())
                                             .redirect(
                                                $$0,
                                                $$2xx -> storeData(
                                                   (CommandSourceStack)$$2xx.getSource(),
                                                   $$3.access($$2xx),
                                                   NbtPathArgument.getPath($$2xx, "path"),
                                                   $$1xxx -> IntTag.valueOf((int)($$1xxx * DoubleArgumentType.getDouble($$2xx, "scale"))),
                                                   $$2
                                                )
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("float")
                                    .then(
                                       Commands.argument("scale", DoubleArgumentType.doubleArg())
                                          .redirect(
                                             $$0,
                                             $$2xx -> storeData(
                                                (CommandSourceStack)$$2xx.getSource(),
                                                $$3.access($$2xx),
                                                NbtPathArgument.getPath($$2xx, "path"),
                                                $$1xxx -> FloatTag.valueOf((float)($$1xxx * DoubleArgumentType.getDouble($$2xx, "scale"))),
                                                $$2
                                             )
                                          )
                                    )
                              ))
                           .then(
                              Commands.literal("short")
                                 .then(
                                    Commands.argument("scale", DoubleArgumentType.doubleArg())
                                       .redirect(
                                          $$0,
                                          $$2xx -> storeData(
                                             (CommandSourceStack)$$2xx.getSource(),
                                             $$3.access($$2xx),
                                             NbtPathArgument.getPath($$2xx, "path"),
                                             $$1xxx -> ShortTag.valueOf((short)($$1xxx * DoubleArgumentType.getDouble($$2xx, "scale"))),
                                             $$2
                                          )
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("long")
                              .then(
                                 Commands.argument("scale", DoubleArgumentType.doubleArg())
                                    .redirect(
                                       $$0,
                                       $$2xx -> storeData(
                                          (CommandSourceStack)$$2xx.getSource(),
                                          $$3.access($$2xx),
                                          NbtPathArgument.getPath($$2xx, "path"),
                                          $$1xxx -> LongTag.valueOf((long)($$1xxx * DoubleArgumentType.getDouble($$2xx, "scale"))),
                                          $$2
                                       )
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("double")
                           .then(
                              Commands.argument("scale", DoubleArgumentType.doubleArg())
                                 .redirect(
                                    $$0,
                                    $$2xx -> storeData(
                                       (CommandSourceStack)$$2xx.getSource(),
                                       $$3.access($$2xx),
                                       NbtPathArgument.getPath($$2xx, "path"),
                                       $$1xxx -> DoubleTag.valueOf($$1xxx * DoubleArgumentType.getDouble($$2xx, "scale")),
                                       $$2
                                    )
                                 )
                           )
                     ))
                  .then(
                     Commands.literal("byte")
                        .then(
                           Commands.argument("scale", DoubleArgumentType.doubleArg())
                              .redirect(
                                 $$0,
                                 $$2xx -> storeData(
                                    (CommandSourceStack)$$2xx.getSource(),
                                    $$3.access($$2xx),
                                    NbtPathArgument.getPath($$2xx, "path"),
                                    $$1xxx -> ByteTag.valueOf((byte)($$1xxx * DoubleArgumentType.getDouble($$2xx, "scale"))),
                                    $$2
                                 )
                              )
                        )
                  )
            )
         );
      }

      return $$1;
   }

   private static CommandSourceStack storeValue(CommandSourceStack $$0, Collection<ScoreHolder> $$1, Objective $$2, boolean $$3) {
      Scoreboard $$4 = $$0.getServer().getScoreboard();
      return $$0.withCallback(($$4x, $$5) -> {
         for (ScoreHolder $$6 : $$1) {
            ScoreAccess $$7 = $$4.getOrCreatePlayerScore($$6, $$2);
            int $$8 = $$3 ? $$5 : ($$4x ? 1 : 0);
            $$7.set($$8);
         }
      }, CommandResultCallback::chain);
   }

   private static CommandSourceStack storeValue(CommandSourceStack $$0, CustomBossEvent $$1, boolean $$2, boolean $$3) {
      return $$0.withCallback(($$3x, $$4) -> {
         int $$5 = $$3 ? $$4 : ($$3x ? 1 : 0);
         if ($$2) {
            $$1.setValue($$5);
         } else {
            $$1.setMax($$5);
         }
      }, CommandResultCallback::chain);
   }

   private static CommandSourceStack storeData(CommandSourceStack $$0, DataAccessor $$1, NbtPath $$2, IntFunction<Tag> $$3, boolean $$4) {
      return $$0.withCallback(($$4x, $$5) -> {
         try {
            CompoundTag $$6 = $$1.getData();
            int $$7 = $$4 ? $$5 : ($$4x ? 1 : 0);
            $$2.set($$6, $$3.apply($$7));
            $$1.setData($$6);
         } catch (CommandSyntaxException var8) {
         }
      }, CommandResultCallback::chain);
   }

   private static boolean isChunkLoaded(ServerLevel $$0, BlockPos $$1) {
      ChunkPos $$2 = new ChunkPos($$1);
      LevelChunk $$3 = $$0.getChunkSource().getChunkNow($$2.x, $$2.z);
      return $$3 == null ? false : $$3.getFullStatus() == FullChunkStatus.ENTITY_TICKING && $$0.areEntitiesLoaded($$2.toLong());
   }

   private static ArgumentBuilder<CommandSourceStack, ?> addConditionals(
      CommandNode<CommandSourceStack> $$0, LiteralArgumentBuilder<CommandSourceStack> $$1, boolean $$2, CommandBuildContext $$3
   ) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)$$1.then(
                                       Commands.literal("block")
                                          .then(
                                             Commands.argument("pos", BlockPosArgument.blockPos())
                                                .then(
                                                   addConditional(
                                                      $$0,
                                                      Commands.argument("block", BlockPredicateArgument.blockPredicate($$3)),
                                                      $$2,
                                                      $$0x -> BlockPredicateArgument.getBlockPredicate($$0x, "block")
                                                         .test(
                                                            new BlockInWorld(
                                                               ((CommandSourceStack)$$0x.getSource()).getLevel(),
                                                               BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                               true
                                                            )
                                                         )
                                                   )
                                                )
                                          )
                                    ))
                                    .then(
                                       Commands.literal("biome")
                                          .then(
                                             Commands.argument("pos", BlockPosArgument.blockPos())
                                                .then(
                                                   addConditional(
                                                      $$0,
                                                      Commands.argument("biome", ResourceOrTagArgument.resourceOrTag($$3, Registries.BIOME)),
                                                      $$2,
                                                      $$0x -> ResourceOrTagArgument.getResourceOrTag($$0x, "biome", Registries.BIOME)
                                                         .test(
                                                            ((CommandSourceStack)$$0x.getSource())
                                                               .getLevel()
                                                               .getBiome(BlockPosArgument.getLoadedBlockPos($$0x, "pos"))
                                                         )
                                                   )
                                                )
                                          )
                                    ))
                                 .then(
                                    Commands.literal("loaded")
                                       .then(
                                          addConditional(
                                             $$0,
                                             Commands.argument("pos", BlockPosArgument.blockPos()),
                                             $$2,
                                             $$0x -> isChunkLoaded(((CommandSourceStack)$$0x.getSource()).getLevel(), BlockPosArgument.getBlockPos($$0x, "pos"))
                                          )
                                       )
                                 ))
                              .then(
                                 Commands.literal("dimension")
                                    .then(
                                       addConditional(
                                          $$0,
                                          Commands.argument("dimension", DimensionArgument.dimension()),
                                          $$2,
                                          $$0x -> DimensionArgument.getDimension($$0x, "dimension") == ((CommandSourceStack)$$0x.getSource()).getLevel()
                                       )
                                    )
                              ))
                           .then(
                              Commands.literal("score")
                                 .then(
                                    Commands.argument("target", ScoreHolderArgument.scoreHolder())
                                       .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                       .then(
                                          ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                                               "targetObjective", ObjectiveArgument.objective()
                                                            )
                                                            .then(
                                                               Commands.literal("=")
                                                                  .then(
                                                                     Commands.argument("source", ScoreHolderArgument.scoreHolder())
                                                                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                                        .then(
                                                                           addConditional(
                                                                              $$0,
                                                                              Commands.argument("sourceObjective", ObjectiveArgument.objective()),
                                                                              $$2,
                                                                              $$0x -> checkScore(
                                                                                 $$0x, (ExecuteCommand.IntBiPredicate)(($$0xx, $$1x) -> $$0xx == $$1x)
                                                                              )
                                                                           )
                                                                        )
                                                                  )
                                                            ))
                                                         .then(
                                                            Commands.literal("<")
                                                               .then(
                                                                  Commands.argument("source", ScoreHolderArgument.scoreHolder())
                                                                     .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                                     .then(
                                                                        addConditional(
                                                                           $$0,
                                                                           Commands.argument("sourceObjective", ObjectiveArgument.objective()),
                                                                           $$2,
                                                                           $$0x -> checkScore(
                                                                              $$0x, (ExecuteCommand.IntBiPredicate)(($$0xx, $$1x) -> $$0xx < $$1x)
                                                                           )
                                                                        )
                                                                     )
                                                               )
                                                         ))
                                                      .then(
                                                         Commands.literal("<=")
                                                            .then(
                                                               Commands.argument("source", ScoreHolderArgument.scoreHolder())
                                                                  .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                                  .then(
                                                                     addConditional(
                                                                        $$0,
                                                                        Commands.argument("sourceObjective", ObjectiveArgument.objective()),
                                                                        $$2,
                                                                        $$0x -> checkScore(
                                                                           $$0x, (ExecuteCommand.IntBiPredicate)(($$0xx, $$1x) -> $$0xx <= $$1x)
                                                                        )
                                                                     )
                                                                  )
                                                            )
                                                      ))
                                                   .then(
                                                      Commands.literal(">")
                                                         .then(
                                                            Commands.argument("source", ScoreHolderArgument.scoreHolder())
                                                               .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                               .then(
                                                                  addConditional(
                                                                     $$0,
                                                                     Commands.argument("sourceObjective", ObjectiveArgument.objective()),
                                                                     $$2,
                                                                     $$0x -> checkScore($$0x, (ExecuteCommand.IntBiPredicate)(($$0xx, $$1x) -> $$0xx > $$1x))
                                                                  )
                                                               )
                                                         )
                                                   ))
                                                .then(
                                                   Commands.literal(">=")
                                                      .then(
                                                         Commands.argument("source", ScoreHolderArgument.scoreHolder())
                                                            .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                            .then(
                                                               addConditional(
                                                                  $$0,
                                                                  Commands.argument("sourceObjective", ObjectiveArgument.objective()),
                                                                  $$2,
                                                                  $$0x -> checkScore($$0x, (ExecuteCommand.IntBiPredicate)(($$0xx, $$1x) -> $$0xx >= $$1x))
                                                               )
                                                            )
                                                      )
                                                ))
                                             .then(
                                                Commands.literal("matches")
                                                   .then(
                                                      addConditional(
                                                         $$0,
                                                         Commands.argument("range", RangeArgument.intRange()),
                                                         $$2,
                                                         $$0x -> checkScore($$0x, Ints.getRange($$0x, "range"))
                                                      )
                                                   )
                                             )
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("blocks")
                              .then(
                                 Commands.argument("start", BlockPosArgument.blockPos())
                                    .then(
                                       Commands.argument("end", BlockPosArgument.blockPos())
                                          .then(
                                             ((RequiredArgumentBuilder)Commands.argument("destination", BlockPosArgument.blockPos())
                                                   .then(addIfBlocksConditional($$0, Commands.literal("all"), $$2, false)))
                                                .then(addIfBlocksConditional($$0, Commands.literal("masked"), $$2, true))
                                          )
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("entity")
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("entities", EntityArgument.entities())
                                    .fork($$0, $$1x -> expect($$1x, $$2, !EntityArgument.getOptionalEntities($$1x, "entities").isEmpty())))
                                 .executes(createNumericConditionalHandler($$2, $$0x -> EntityArgument.getOptionalEntities($$0x, "entities").size()))
                           )
                     ))
                  .then(
                     Commands.literal("predicate")
                        .then(
                           addConditional(
                              $$0,
                              Commands.argument("predicate", ResourceOrIdArgument.lootPredicate($$3)),
                              $$2,
                              $$0x -> checkCustomPredicate((CommandSourceStack)$$0x.getSource(), ResourceOrIdArgument.getLootPredicate($$0x, "predicate"))
                           )
                        )
                  ))
               .then(
                  Commands.literal("function")
                     .then(
                        Commands.argument("name", FunctionArgument.functions())
                           .suggests(FunctionCommand.SUGGEST_FUNCTION)
                           .fork($$0, new ExecuteCommand.ExecuteIfFunctionCustomModifier($$2))
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)Commands.literal("items")
                     .then(
                        Commands.literal("entity")
                           .then(
                              Commands.argument("entities", EntityArgument.entities())
                                 .then(
                                    Commands.argument("slots", SlotsArgument.slots())
                                       .then(
                                          ((RequiredArgumentBuilder)Commands.argument("item_predicate", ItemPredicateArgument.itemPredicate($$3))
                                                .fork(
                                                   $$0,
                                                   $$1x -> expect(
                                                      $$1x,
                                                      $$2,
                                                      countItems(
                                                            EntityArgument.getEntities($$1x, "entities"),
                                                            SlotsArgument.getSlots($$1x, "slots"),
                                                            ItemPredicateArgument.getItemPredicate($$1x, "item_predicate")
                                                         )
                                                         > 0
                                                   )
                                                ))
                                             .executes(
                                                createNumericConditionalHandler(
                                                   $$2,
                                                   $$0x -> countItems(
                                                      EntityArgument.getEntities($$0x, "entities"),
                                                      SlotsArgument.getSlots($$0x, "slots"),
                                                      ItemPredicateArgument.getItemPredicate($$0x, "item_predicate")
                                                   )
                                                )
                                             )
                                       )
                                 )
                           )
                     ))
                  .then(
                     Commands.literal("block")
                        .then(
                           Commands.argument("pos", BlockPosArgument.blockPos())
                              .then(
                                 Commands.argument("slots", SlotsArgument.slots())
                                    .then(
                                       ((RequiredArgumentBuilder)Commands.argument("item_predicate", ItemPredicateArgument.itemPredicate($$3))
                                             .fork(
                                                $$0,
                                                $$1x -> expect(
                                                   $$1x,
                                                   $$2,
                                                   countItems(
                                                         (CommandSourceStack)$$1x.getSource(),
                                                         BlockPosArgument.getLoadedBlockPos($$1x, "pos"),
                                                         SlotsArgument.getSlots($$1x, "slots"),
                                                         ItemPredicateArgument.getItemPredicate($$1x, "item_predicate")
                                                      )
                                                      > 0
                                                )
                                             ))
                                          .executes(
                                             createNumericConditionalHandler(
                                                $$2,
                                                $$0x -> countItems(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                   SlotsArgument.getSlots($$0x, "slots"),
                                                   ItemPredicateArgument.getItemPredicate($$0x, "item_predicate")
                                                )
                                             )
                                          )
                                    )
                              )
                        )
                  )
            ))
         .then(
            Commands.literal("stopwatch")
               .then(
                  Commands.argument("id", IdentifierArgument.id())
                     .suggests(StopwatchCommand.SUGGEST_STOPWATCHES)
                     .then(
                        addConditional(
                           $$0, Commands.argument("range", RangeArgument.floatRange()), $$2, $$0x -> checkStopwatch($$0x, Floats.getRange($$0x, "range"))
                        )
                     )
               )
         );

      for (DataCommands.DataProvider $$4 : DataCommands.SOURCE_PROVIDERS) {
         $$1.then(
            $$4.wrap(
               Commands.literal("data"),
               $$3x -> $$3x.then(
                  ((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath())
                        .fork($$0, $$2xx -> expect($$2xx, $$2, checkMatchingData($$4.access($$2xx), NbtPathArgument.getPath($$2xx, "path")) > 0)))
                     .executes(createNumericConditionalHandler($$2, $$1xx -> checkMatchingData($$4.access($$1xx), NbtPathArgument.getPath($$1xx, "path"))))
               )
            )
         );
      }

      return $$1;
   }

   private static int countItems(Iterable<? extends SlotProvider> $$0, SlotRange $$1, Predicate<ItemStack> $$2) {
      int $$3 = 0;

      for (SlotProvider $$4 : $$0) {
         IntList $$5 = $$1.slots();

         for (int $$6 = 0; $$6 < $$5.size(); $$6++) {
            int $$7 = $$5.getInt($$6);
            SlotAccess $$8 = $$4.getSlot($$7);
            if ($$8 != null) {
               ItemStack $$9 = $$8.get();
               if ($$2.test($$9)) {
                  $$3 += $$9.getCount();
               }
            }
         }
      }

      return $$3;
   }

   private static int countItems(CommandSourceStack $$0, BlockPos $$1, SlotRange $$2, Predicate<ItemStack> $$3) throws CommandSyntaxException {
      int $$4 = 0;
      Container $$5 = ItemCommands.getContainer($$0, $$1, ItemCommands.ERROR_SOURCE_NOT_A_CONTAINER);
      int $$6 = $$5.getContainerSize();
      IntList $$7 = $$2.slots();

      for (int $$8 = 0; $$8 < $$7.size(); $$8++) {
         int $$9 = $$7.getInt($$8);
         if ($$9 >= 0 && $$9 < $$6) {
            ItemStack $$10 = $$5.getItem($$9);
            if ($$3.test($$10)) {
               $$4 += $$10.getCount();
            }
         }
      }

      return $$4;
   }

   private static Command<CommandSourceStack> createNumericConditionalHandler(boolean $$0, ExecuteCommand.CommandNumericPredicate $$1) {
      return $$0
         ? $$1x -> {
            int $$2 = $$1.test($$1x);
            if ($$2 > 0) {
               ((CommandSourceStack)$$1x.getSource())
                  .sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", new Object[]{$$2}), false);
               return $$2;
            } else {
               throw ERROR_CONDITIONAL_FAILED.create();
            }
         }
         : $$1x -> {
            int $$2 = $$1.test($$1x);
            if ($$2 == 0) {
               ((CommandSourceStack)$$1x.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
               return 1;
            } else {
               throw ERROR_CONDITIONAL_FAILED_COUNT.create($$2);
            }
         };
   }

   private static int checkMatchingData(DataAccessor $$0, NbtPath $$1) throws CommandSyntaxException {
      return $$1.countMatching($$0.getData());
   }

   private static boolean checkScore(CommandContext<CommandSourceStack> $$0, ExecuteCommand.IntBiPredicate $$1) throws CommandSyntaxException {
      ScoreHolder $$2 = ScoreHolderArgument.getName($$0, "target");
      Objective $$3 = ObjectiveArgument.getObjective($$0, "targetObjective");
      ScoreHolder $$4 = ScoreHolderArgument.getName($$0, "source");
      Objective $$5 = ObjectiveArgument.getObjective($$0, "sourceObjective");
      Scoreboard $$6 = ((CommandSourceStack)$$0.getSource()).getServer().getScoreboard();
      ReadOnlyScoreInfo $$7 = $$6.getPlayerScoreInfo($$2, $$3);
      ReadOnlyScoreInfo $$8 = $$6.getPlayerScoreInfo($$4, $$5);
      return $$7 != null && $$8 != null ? $$1.test($$7.value(), $$8.value()) : false;
   }

   private static boolean checkScore(CommandContext<CommandSourceStack> $$0, net.minecraft.advancements.criterion.MinMaxBounds.Ints $$1) throws CommandSyntaxException {
      ScoreHolder $$2 = ScoreHolderArgument.getName($$0, "target");
      Objective $$3 = ObjectiveArgument.getObjective($$0, "targetObjective");
      Scoreboard $$4 = ((CommandSourceStack)$$0.getSource()).getServer().getScoreboard();
      ReadOnlyScoreInfo $$5 = $$4.getPlayerScoreInfo($$2, $$3);
      return $$5 == null ? false : $$1.matches($$5.value());
   }

   private static boolean checkStopwatch(CommandContext<CommandSourceStack> $$0, Doubles $$1) throws CommandSyntaxException {
      Identifier $$2 = IdentifierArgument.getId($$0, "id");
      Stopwatches $$3 = ((CommandSourceStack)$$0.getSource()).getServer().getStopwatches();
      Stopwatch $$4 = $$3.get($$2);
      if ($$4 == null) {
         throw StopwatchCommand.ERROR_DOES_NOT_EXIST.create($$2);
      } else {
         long $$5 = Stopwatches.currentTime();
         double $$6 = $$4.elapsedSeconds($$5);
         return $$1.matches($$6);
      }
   }

   private static boolean checkCustomPredicate(CommandSourceStack $$0, Holder<LootItemCondition> $$1) {
      ServerLevel $$2 = $$0.getLevel();
      LootParams $$3 = new Builder($$2)
         .withParameter(LootContextParams.ORIGIN, $$0.getPosition())
         .withOptionalParameter(LootContextParams.THIS_ENTITY, $$0.getEntity())
         .create(LootContextParamSets.COMMAND);
      LootContext $$4 = new net.minecraft.world.level.storage.loot.LootContext.Builder($$3).create(Optional.empty());
      $$4.pushVisitedElement(LootContext.createVisitedEntry((LootItemCondition)$$1.value()));
      return ((LootItemCondition)$$1.value()).test($$4);
   }

   private static Collection<CommandSourceStack> expect(CommandContext<CommandSourceStack> $$0, boolean $$1, boolean $$2) {
      return (Collection<CommandSourceStack>)($$2 == $$1 ? Collections.singleton((CommandSourceStack)$$0.getSource()) : Collections.emptyList());
   }

   private static ArgumentBuilder<CommandSourceStack, ?> addConditional(
      CommandNode<CommandSourceStack> $$0, ArgumentBuilder<CommandSourceStack, ?> $$1, boolean $$2, ExecuteCommand.CommandPredicate $$3
   ) {
      return $$1.fork($$0, $$2x -> expect($$2x, $$2, $$3.test($$2x))).executes($$2x -> {
         if ($$2 == $$3.test($$2x)) {
            ((CommandSourceStack)$$2x.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
            return 1;
         } else {
            throw ERROR_CONDITIONAL_FAILED.create();
         }
      });
   }

   private static ArgumentBuilder<CommandSourceStack, ?> addIfBlocksConditional(
      CommandNode<CommandSourceStack> $$0, ArgumentBuilder<CommandSourceStack, ?> $$1, boolean $$2, boolean $$3
   ) {
      return $$1.fork($$0, $$2x -> expect($$2x, $$2, checkRegions($$2x, $$3).isPresent()))
         .executes($$2 ? $$1x -> checkIfRegions($$1x, $$3) : $$1x -> checkUnlessRegions($$1x, $$3));
   }

   private static int checkIfRegions(CommandContext<CommandSourceStack> $$0, boolean $$1) throws CommandSyntaxException {
      OptionalInt $$2 = checkRegions($$0, $$1);
      if ($$2.isPresent()) {
         ((CommandSourceStack)$$0.getSource())
            .sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", new Object[]{$$2.getAsInt()}), false);
         return $$2.getAsInt();
      } else {
         throw ERROR_CONDITIONAL_FAILED.create();
      }
   }

   private static int checkUnlessRegions(CommandContext<CommandSourceStack> $$0, boolean $$1) throws CommandSyntaxException {
      OptionalInt $$2 = checkRegions($$0, $$1);
      if ($$2.isPresent()) {
         throw ERROR_CONDITIONAL_FAILED_COUNT.create($$2.getAsInt());
      } else {
         ((CommandSourceStack)$$0.getSource()).sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
         return 1;
      }
   }

   private static OptionalInt checkRegions(CommandContext<CommandSourceStack> $$0, boolean $$1) throws CommandSyntaxException {
      return checkRegions(
         ((CommandSourceStack)$$0.getSource()).getLevel(),
         BlockPosArgument.getLoadedBlockPos($$0, "start"),
         BlockPosArgument.getLoadedBlockPos($$0, "end"),
         BlockPosArgument.getLoadedBlockPos($$0, "destination"),
         $$1
      );
   }

   private static OptionalInt checkRegions(ServerLevel $$0, BlockPos $$1, BlockPos $$2, BlockPos $$3, boolean $$4) throws CommandSyntaxException {
      BoundingBox $$5 = BoundingBox.fromCorners($$1, $$2);
      BoundingBox $$6 = BoundingBox.fromCorners($$3, $$3.offset($$5.getLength()));
      BlockPos $$7 = new BlockPos($$6.minX() - $$5.minX(), $$6.minY() - $$5.minY(), $$6.minZ() - $$5.minZ());
      int $$8 = $$5.getXSpan() * $$5.getYSpan() * $$5.getZSpan();
      if ($$8 > 32768) {
         throw ERROR_AREA_TOO_LARGE.create(32768, $$8);
      } else {
         int $$9 = 0;
         RegistryAccess $$10 = $$0.registryAccess();
         ScopedCollector $$11 = new ScopedCollector(LOGGER);

         OptionalInt var27;
         label98: {
            OptionalInt var30;
            label97: {
               label96: {
                  label95: {
                     OptionalInt var24;
                     label94: {
                        try {
                           for (int $$12 = $$5.minZ(); $$12 <= $$5.maxZ(); $$12++) {
                              for (int $$13 = $$5.minY(); $$13 <= $$5.maxY(); $$13++) {
                                 for (int $$14 = $$5.minX(); $$14 <= $$5.maxX(); $$14++) {
                                    BlockPos $$15 = new BlockPos($$14, $$13, $$12);
                                    BlockPos $$16 = $$15.offset($$7);
                                    BlockState $$17 = $$0.getBlockState($$15);
                                    if (!$$4 || !$$17.is(Blocks.AIR)) {
                                       if ($$17 != $$0.getBlockState($$16)) {
                                          var27 = OptionalInt.empty();
                                          break label98;
                                       }

                                       BlockEntity $$18 = $$0.getBlockEntity($$15);
                                       BlockEntity $$19 = $$0.getBlockEntity($$16);
                                       if ($$18 != null) {
                                          if ($$19 == null) {
                                             var30 = OptionalInt.empty();
                                             break label97;
                                          }

                                          if ($$19.getType() != $$18.getType()) {
                                             var30 = OptionalInt.empty();
                                             break label96;
                                          }

                                          if (!$$18.components().equals($$19.components())) {
                                             var30 = OptionalInt.empty();
                                             break label95;
                                          }

                                          TagValueOutput $$20 = TagValueOutput.createWithContext($$11.forChild($$18.problemPath()), $$10);
                                          $$18.saveCustomOnly($$20);
                                          CompoundTag $$21 = $$20.buildResult();
                                          TagValueOutput $$22 = TagValueOutput.createWithContext($$11.forChild($$19.problemPath()), $$10);
                                          $$19.saveCustomOnly($$22);
                                          CompoundTag $$23 = $$22.buildResult();
                                          if (!$$21.equals($$23)) {
                                             var24 = OptionalInt.empty();
                                             break label94;
                                          }
                                       }

                                       $$9++;
                                    }
                                 }
                              }
                           }
                        } catch (Throwable var26) {
                           try {
                              $$11.close();
                           } catch (Throwable var25) {
                              var26.addSuppressed(var25);
                           }

                           throw var26;
                        }

                        $$11.close();
                        return OptionalInt.of($$9);
                     }

                     $$11.close();
                     return var24;
                  }

                  $$11.close();
                  return var30;
               }

               $$11.close();
               return var30;
            }

            $$11.close();
            return var30;
         }

         $$11.close();
         return var27;
      }
   }

   private static RedirectModifier<CommandSourceStack> expandOneToOneEntityRelation(Function<Entity, Optional<Entity>> $$0) {
      return $$1 -> {
         CommandSourceStack $$2 = (CommandSourceStack)$$1.getSource();
         Entity $$3 = $$2.getEntity();
         return (Collection)($$3 == null
            ? List.of()
            : $$0.apply($$3).filter($$0xx -> !$$0xx.isRemoved()).map($$1x -> List.of($$2.withEntity($$1x))).orElse(List.of()));
      };
   }

   private static RedirectModifier<CommandSourceStack> expandOneToManyEntityRelation(Function<Entity, Stream<Entity>> $$0) {
      return $$1 -> {
         CommandSourceStack $$2 = (CommandSourceStack)$$1.getSource();
         Entity $$3 = $$2.getEntity();
         return $$3 == null ? List.of() : $$0.apply($$3).filter($$0xx -> !$$0xx.isRemoved()).map($$2::withEntity).toList();
      };
   }

   private static LiteralArgumentBuilder<CommandSourceStack> createRelationOperations(
      CommandNode<CommandSourceStack> $$0, LiteralArgumentBuilder<CommandSourceStack> $$1
   ) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)$$1.then(
                              Commands.literal("owner")
                                 .fork(
                                    $$0,
                                    expandOneToOneEntityRelation(
                                       $$0x -> $$0x instanceof OwnableEntity $$1x ? Optional.ofNullable($$1x.getOwner()) : Optional.empty()
                                    )
                                 )
                           ))
                           .then(
                              Commands.literal("leasher")
                                 .fork(
                                    $$0,
                                    expandOneToOneEntityRelation(
                                       $$0x -> $$0x instanceof Leashable $$1x ? Optional.ofNullable($$1x.getLeashHolder()) : Optional.empty()
                                    )
                                 )
                           ))
                        .then(
                           Commands.literal("target")
                              .fork(
                                 $$0,
                                 expandOneToOneEntityRelation($$0x -> $$0x instanceof Targeting $$1x ? Optional.ofNullable($$1x.getTarget()) : Optional.empty())
                              )
                        ))
                     .then(
                        Commands.literal("attacker")
                           .fork(
                              $$0,
                              expandOneToOneEntityRelation(
                                 $$0x -> $$0x instanceof Attackable $$1x ? Optional.ofNullable($$1x.getLastAttacker()) : Optional.empty()
                              )
                           )
                     ))
                  .then(Commands.literal("vehicle").fork($$0, expandOneToOneEntityRelation($$0x -> Optional.ofNullable($$0x.getVehicle())))))
               .then(Commands.literal("controller").fork($$0, expandOneToOneEntityRelation($$0x -> Optional.ofNullable($$0x.getControllingPassenger())))))
            .then(
               Commands.literal("origin")
                  .fork(
                     $$0, expandOneToOneEntityRelation($$0x -> $$0x instanceof TraceableEntity $$1x ? Optional.ofNullable($$1x.getOwner()) : Optional.empty())
                  )
            ))
         .then(Commands.literal("passengers").fork($$0, expandOneToManyEntityRelation($$0x -> $$0x.getPassengers().stream())));
   }

   private static CommandSourceStack spawnEntityAndRedirect(CommandSourceStack $$0, Reference<EntityType<?>> $$1) throws CommandSyntaxException {
      Entity $$2 = SummonCommand.createEntity($$0, $$1, $$0.getPosition(), new CompoundTag(), true);
      return $$0.withEntity($$2);
   }

   public static <T extends ExecutionCommandSource<T>> void scheduleFunctionConditionsAndTest(
      T $$0,
      List<T> $$1,
      Function<T, T> $$2,
      IntPredicate $$3,
      ContextChain<T> $$4,
      CompoundTag $$5,
      ExecutionControl<T> $$6,
      InCommandFunction<CommandContext<T>, Collection<CommandFunction<T>>> $$7,
      ChainModifiers $$8
   ) {
      List<T> $$9 = new ArrayList<>($$1.size());

      Collection<CommandFunction<T>> $$10;
      try {
         $$10 = $$7.apply($$4.getTopContext().copyFor($$0));
      } catch (CommandSyntaxException var18) {
         $$0.handleError(var18, $$8.isForked(), $$6.tracer());
         return;
      }

      int $$13 = $$10.size();
      if ($$13 != 0) {
         List<InstantiatedFunction<T>> $$14 = new ArrayList<>($$13);

         try {
            for (CommandFunction<T> $$15 : $$10) {
               try {
                  $$14.add($$15.instantiate($$5, $$0.dispatcher()));
               } catch (FunctionInstantiationException var17) {
                  throw ERROR_FUNCTION_CONDITION_INSTANTATION_FAILURE.create($$15.id(), var17.messageComponent());
               }
            }
         } catch (CommandSyntaxException var19) {
            $$0.handleError(var19, $$8.isForked(), $$6.tracer());
         }

         for (T $$18 : $$1) {
            T $$19 = (T)$$2.apply((T)$$18.clearCallbacks());
            CommandResultCallback $$20 = ($$3x, $$4x) -> {
               if ($$3.test($$4x)) {
                  $$9.add($$18);
               }
            };
            $$6.queueNext(new IsolatedCall($$2x -> {
               for (InstantiatedFunction<T> $$3x : $$14) {
                  $$2x.queueNext(new CallFunction($$3x, $$2x.currentFrame().returnValueConsumer(), true).bind($$19));
               }

               $$2x.queueNext(FallthroughTask.instance());
            }, $$20));
         }

         ContextChain<T> $$21 = $$4.nextStage();
         String $$22 = $$4.getTopContext().getInput();
         $$6.queueNext(new Continuation($$22, $$21, $$8, $$0, $$9));
      }
   }

   @FunctionalInterface
   interface CommandNumericPredicate {
      int test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface CommandPredicate {
      boolean test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
   }

   static class ExecuteIfFunctionCustomModifier implements ModifierAdapter<CommandSourceStack> {
      private final IntPredicate check;

      ExecuteIfFunctionCustomModifier(boolean $$0) {
         this.check = $$0 ? $$0x -> $$0x != 0 : $$0x -> $$0x == 0;
      }

      public void apply(
         CommandSourceStack $$0,
         List<CommandSourceStack> $$1,
         ContextChain<CommandSourceStack> $$2,
         ChainModifiers $$3,
         ExecutionControl<CommandSourceStack> $$4
      ) {
         ExecuteCommand.scheduleFunctionConditionsAndTest(
            $$0, $$1, FunctionCommand::modifySenderForExecution, this.check, $$2, null, $$4, $$0x -> FunctionArgument.getFunctions($$0x, "name"), $$3
         );
      }
   }

   @FunctionalInterface
   interface IntBiPredicate {
      boolean test(int var1, int var2);
   }
}
