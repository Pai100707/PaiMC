package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.commands.arguments.OperationArgument.Operation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jspecify.annotations.Nullable;

public class ScoreboardCommand {
   private static final SimpleCommandExceptionType ERROR_OBJECTIVE_ALREADY_EXISTS = new SimpleCommandExceptionType(
      Component.translatable("commands.scoreboard.objectives.add.duplicate")
   );
   private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_EMPTY = new SimpleCommandExceptionType(
      Component.translatable("commands.scoreboard.objectives.display.alreadyEmpty")
   );
   private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_SET = new SimpleCommandExceptionType(
      Component.translatable("commands.scoreboard.objectives.display.alreadySet")
   );
   private static final SimpleCommandExceptionType ERROR_TRIGGER_ALREADY_ENABLED = new SimpleCommandExceptionType(
      Component.translatable("commands.scoreboard.players.enable.failed")
   );
   private static final SimpleCommandExceptionType ERROR_NOT_TRIGGER = new SimpleCommandExceptionType(
      Component.translatable("commands.scoreboard.players.enable.invalid")
   );
   private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.scoreboard.players.get.null", new Object[]{$$0, $$1})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("scoreboard")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .then(
                  ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("objectives")
                                 .then(Commands.literal("list").executes($$0x -> listObjectives((CommandSourceStack)$$0x.getSource()))))
                              .then(
                                 Commands.literal("add")
                                    .then(
                                       Commands.argument("objective", StringArgumentType.word())
                                          .then(
                                             ((RequiredArgumentBuilder)Commands.argument("criteria", ObjectiveCriteriaArgument.criteria())
                                                   .executes(
                                                      $$0x -> addObjective(
                                                         (CommandSourceStack)$$0x.getSource(),
                                                         StringArgumentType.getString($$0x, "objective"),
                                                         ObjectiveCriteriaArgument.getCriteria($$0x, "criteria"),
                                                         Component.literal(StringArgumentType.getString($$0x, "objective"))
                                                      )
                                                   ))
                                                .then(
                                                   Commands.argument("displayName", ComponentArgument.textComponent($$1))
                                                      .executes(
                                                         $$0x -> addObjective(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            StringArgumentType.getString($$0x, "objective"),
                                                            ObjectiveCriteriaArgument.getCriteria($$0x, "criteria"),
                                                            ComponentArgument.getResolvedComponent($$0x, "displayName")
                                                         )
                                                      )
                                                )
                                          )
                                    )
                              ))
                           .then(
                              Commands.literal("modify")
                                 .then(
                                    ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                                   "objective", ObjectiveArgument.objective()
                                                )
                                                .then(
                                                   Commands.literal("displayname")
                                                      .then(
                                                         Commands.argument("displayName", ComponentArgument.textComponent($$1))
                                                            .executes(
                                                               $$0x -> setDisplayName(
                                                                  (CommandSourceStack)$$0x.getSource(),
                                                                  ObjectiveArgument.getObjective($$0x, "objective"),
                                                                  ComponentArgument.getResolvedComponent($$0x, "displayName")
                                                               )
                                                            )
                                                      )
                                                ))
                                             .then(createRenderTypeModify()))
                                          .then(
                                             Commands.literal("displayautoupdate")
                                                .then(
                                                   Commands.argument("value", BoolArgumentType.bool())
                                                      .executes(
                                                         $$0x -> setDisplayAutoUpdate(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            ObjectiveArgument.getObjective($$0x, "objective"),
                                                            BoolArgumentType.getBool($$0x, "value")
                                                         )
                                                      )
                                                )
                                          ))
                                       .then(
                                          addNumberFormats(
                                             $$1,
                                             Commands.literal("numberformat"),
                                             ($$0x, $$1x) -> setObjectiveFormat(
                                                (CommandSourceStack)$$0x.getSource(), ObjectiveArgument.getObjective($$0x, "objective"), $$1x
                                             )
                                          )
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("remove")
                              .then(
                                 Commands.argument("objective", ObjectiveArgument.objective())
                                    .executes($$0x -> removeObjective((CommandSourceStack)$$0x.getSource(), ObjectiveArgument.getObjective($$0x, "objective")))
                              )
                        ))
                     .then(
                        Commands.literal("setdisplay")
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("slot", ScoreboardSlotArgument.displaySlot())
                                    .executes(
                                       $$0x -> clearDisplaySlot((CommandSourceStack)$$0x.getSource(), ScoreboardSlotArgument.getDisplaySlot($$0x, "slot"))
                                    ))
                                 .then(
                                    Commands.argument("objective", ObjectiveArgument.objective())
                                       .executes(
                                          $$0x -> setDisplaySlot(
                                             (CommandSourceStack)$$0x.getSource(),
                                             ScoreboardSlotArgument.getDisplaySlot($$0x, "slot"),
                                             ObjectiveArgument.getObjective($$0x, "objective")
                                          )
                                       )
                                 )
                           )
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                             "players"
                                          )
                                          .then(
                                             ((LiteralArgumentBuilder)Commands.literal("list")
                                                   .executes($$0x -> listTrackedPlayers((CommandSourceStack)$$0x.getSource())))
                                                .then(
                                                   Commands.argument("target", ScoreHolderArgument.scoreHolder())
                                                      .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                      .executes(
                                                         $$0x -> listTrackedPlayerScores(
                                                            (CommandSourceStack)$$0x.getSource(), ScoreHolderArgument.getName($$0x, "target")
                                                         )
                                                      )
                                                )
                                          ))
                                       .then(
                                          Commands.literal("set")
                                             .then(
                                                Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                                   .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                   .then(
                                                      Commands.argument("objective", ObjectiveArgument.objective())
                                                         .then(
                                                            Commands.argument("score", IntegerArgumentType.integer())
                                                               .executes(
                                                                  $$0x -> setScore(
                                                                     (CommandSourceStack)$$0x.getSource(),
                                                                     ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets"),
                                                                     ObjectiveArgument.getWritableObjective($$0x, "objective"),
                                                                     IntegerArgumentType.getInteger($$0x, "score")
                                                                  )
                                                               )
                                                         )
                                                   )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("get")
                                          .then(
                                             Commands.argument("target", ScoreHolderArgument.scoreHolder())
                                                .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                .then(
                                                   Commands.argument("objective", ObjectiveArgument.objective())
                                                      .executes(
                                                         $$0x -> getScore(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            ScoreHolderArgument.getName($$0x, "target"),
                                                            ObjectiveArgument.getObjective($$0x, "objective")
                                                         )
                                                      )
                                                )
                                          )
                                    ))
                                 .then(
                                    Commands.literal("add")
                                       .then(
                                          Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                             .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                             .then(
                                                Commands.argument("objective", ObjectiveArgument.objective())
                                                   .then(
                                                      Commands.argument("score", IntegerArgumentType.integer(0))
                                                         .executes(
                                                            $$0x -> addScore(
                                                               (CommandSourceStack)$$0x.getSource(),
                                                               ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets"),
                                                               ObjectiveArgument.getWritableObjective($$0x, "objective"),
                                                               IntegerArgumentType.getInteger($$0x, "score")
                                                            )
                                                         )
                                                   )
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("remove")
                                    .then(
                                       Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                          .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                          .then(
                                             Commands.argument("objective", ObjectiveArgument.objective())
                                                .then(
                                                   Commands.argument("score", IntegerArgumentType.integer(0))
                                                      .executes(
                                                         $$0x -> removeScore(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets"),
                                                            ObjectiveArgument.getWritableObjective($$0x, "objective"),
                                                            IntegerArgumentType.getInteger($$0x, "score")
                                                         )
                                                      )
                                                )
                                          )
                                    )
                              ))
                           .then(
                              Commands.literal("reset")
                                 .then(
                                    ((RequiredArgumentBuilder)Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                          .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                          .executes(
                                             $$0x -> resetScores(
                                                (CommandSourceStack)$$0x.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets")
                                             )
                                          ))
                                       .then(
                                          Commands.argument("objective", ObjectiveArgument.objective())
                                             .executes(
                                                $$0x -> resetScore(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets"),
                                                   ObjectiveArgument.getObjective($$0x, "objective")
                                                )
                                             )
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("enable")
                              .then(
                                 Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                    .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                    .then(
                                       Commands.argument("objective", ObjectiveArgument.objective())
                                          .suggests(
                                             ($$0x, $$1x) -> suggestTriggers(
                                                (CommandSourceStack)$$0x.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets"), $$1x
                                             )
                                          )
                                          .executes(
                                             $$0x -> enableTrigger(
                                                (CommandSourceStack)$$0x.getSource(),
                                                ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets"),
                                                ObjectiveArgument.getObjective($$0x, "objective")
                                             )
                                          )
                                    )
                              )
                        ))
                     .then(
                        ((LiteralArgumentBuilder)Commands.literal("display")
                              .then(
                                 Commands.literal("name")
                                    .then(
                                       Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                          .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                          .then(
                                             ((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective())
                                                   .then(
                                                      Commands.argument("name", ComponentArgument.textComponent($$1))
                                                         .executes(
                                                            $$0x -> setScoreDisplay(
                                                               (CommandSourceStack)$$0x.getSource(),
                                                               ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets"),
                                                               ObjectiveArgument.getObjective($$0x, "objective"),
                                                               ComponentArgument.getResolvedComponent($$0x, "name")
                                                            )
                                                         )
                                                   ))
                                                .executes(
                                                   $$0x -> setScoreDisplay(
                                                      (CommandSourceStack)$$0x.getSource(),
                                                      ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets"),
                                                      ObjectiveArgument.getObjective($$0x, "objective"),
                                                      null
                                                   )
                                                )
                                          )
                                    )
                              ))
                           .then(
                              Commands.literal("numberformat")
                                 .then(
                                    Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                       .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                       .then(
                                          addNumberFormats(
                                             $$1,
                                             Commands.argument("objective", ObjectiveArgument.objective()),
                                             ($$0x, $$1x) -> setScoreNumberFormat(
                                                (CommandSourceStack)$$0x.getSource(),
                                                ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets"),
                                                ObjectiveArgument.getObjective($$0x, "objective"),
                                                $$1x
                                             )
                                          )
                                       )
                                 )
                           )
                     ))
                  .then(
                     Commands.literal("operation")
                        .then(
                           Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                              .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                              .then(
                                 Commands.argument("targetObjective", ObjectiveArgument.objective())
                                    .then(
                                       Commands.argument("operation", OperationArgument.operation())
                                          .then(
                                             Commands.argument("source", ScoreHolderArgument.scoreHolders())
                                                .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                .then(
                                                   Commands.argument("sourceObjective", ObjectiveArgument.objective())
                                                      .executes(
                                                         $$0x -> performOperation(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "targets"),
                                                            ObjectiveArgument.getWritableObjective($$0x, "targetObjective"),
                                                            OperationArgument.getOperation($$0x, "operation"),
                                                            ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "source"),
                                                            ObjectiveArgument.getObjective($$0x, "sourceObjective")
                                                         )
                                                      )
                                                )
                                          )
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> addNumberFormats(
      CommandBuildContext $$0, ArgumentBuilder<CommandSourceStack, ?> $$1, ScoreboardCommand.NumberFormatCommandExecutor $$2
   ) {
      return $$1.then(Commands.literal("blank").executes($$1x -> $$2.run($$1x, BlankFormat.INSTANCE)))
         .then(Commands.literal("fixed").then(Commands.argument("contents", ComponentArgument.textComponent($$0)).executes($$1x -> {
            Component $$2x = ComponentArgument.getResolvedComponent($$1x, "contents");
            return $$2.run($$1x, new FixedFormat($$2x));
         })))
         .then(Commands.literal("styled").then(Commands.argument("style", StyleArgument.style($$0)).executes($$1x -> {
            Style $$2x = StyleArgument.getStyle($$1x, "style");
            return $$2.run($$1x, new StyledFormat($$2x));
         })))
         .executes($$1x -> $$2.run($$1x, null));
   }

   private static LiteralArgumentBuilder<CommandSourceStack> createRenderTypeModify() {
      LiteralArgumentBuilder<CommandSourceStack> $$0 = Commands.literal("rendertype");

      for (RenderType $$1 : RenderType.values()) {
         $$0.then(
            Commands.literal($$1.getId())
               .executes($$1x -> setRenderType((CommandSourceStack)$$1x.getSource(), ObjectiveArgument.getObjective($$1x, "objective"), $$1))
         );
      }

      return $$0;
   }

   private static CompletableFuture<Suggestions> suggestTriggers(CommandSourceStack $$0, Collection<ScoreHolder> $$1, SuggestionsBuilder $$2) {
      List<String> $$3 = Lists.newArrayList();
      Scoreboard $$4 = $$0.getServer().getScoreboard();

      for (Objective $$5 : $$4.getObjectives()) {
         if ($$5.getCriteria() == ObjectiveCriteria.TRIGGER) {
            boolean $$6 = false;

            for (ScoreHolder $$7 : $$1) {
               ReadOnlyScoreInfo $$8 = $$4.getPlayerScoreInfo($$7, $$5);
               if ($$8 == null || $$8.isLocked()) {
                  $$6 = true;
                  break;
               }
            }

            if ($$6) {
               $$3.add($$5.getName());
            }
         }
      }

      return SharedSuggestionProvider.suggest($$3, $$2);
   }

   private static int getScore(CommandSourceStack $$0, ScoreHolder $$1, Objective $$2) throws CommandSyntaxException {
      Scoreboard $$3 = $$0.getServer().getScoreboard();
      ReadOnlyScoreInfo $$4 = $$3.getPlayerScoreInfo($$1, $$2);
      if ($$4 == null) {
         throw ERROR_NO_VALUE.create($$2.getName(), $$1.getFeedbackDisplayName());
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.get.success", new Object[]{$$1.getFeedbackDisplayName(), $$4.value(), $$2.getFormattedDisplayName()}
            ),
            false
         );
         return $$4.value();
      }
   }

   private static Component getFirstTargetName(Collection<ScoreHolder> $$0) {
      return $$0.iterator().next().getFeedbackDisplayName();
   }

   private static int performOperation(
      CommandSourceStack $$0, Collection<ScoreHolder> $$1, Objective $$2, Operation $$3, Collection<ScoreHolder> $$4, Objective $$5
   ) throws CommandSyntaxException {
      Scoreboard $$6 = $$0.getServer().getScoreboard();
      int $$7 = 0;

      for (ScoreHolder $$8 : $$1) {
         ScoreAccess $$9 = $$6.getOrCreatePlayerScore($$8, $$2);

         for (ScoreHolder $$10 : $$4) {
            ScoreAccess $$11 = $$6.getOrCreatePlayerScore($$10, $$5);
            $$3.apply($$9, $$11);
         }

         $$7 += $$9.get();
      }

      if ($$1.size() == 1) {
         int $$12 = $$7;
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.operation.success.single", new Object[]{$$2.getFormattedDisplayName(), getFirstTargetName($$1), $$12}
            ),
            true
         );
      } else {
         $$0.sendSuccess(
            () -> Component.translatable("commands.scoreboard.players.operation.success.multiple", new Object[]{$$2.getFormattedDisplayName(), $$1.size()}),
            true
         );
      }

      return $$7;
   }

   private static int enableTrigger(CommandSourceStack $$0, Collection<ScoreHolder> $$1, Objective $$2) throws CommandSyntaxException {
      if ($$2.getCriteria() != ObjectiveCriteria.TRIGGER) {
         throw ERROR_NOT_TRIGGER.create();
      } else {
         Scoreboard $$3 = $$0.getServer().getScoreboard();
         int $$4 = 0;

         for (ScoreHolder $$5 : $$1) {
            ScoreAccess $$6 = $$3.getOrCreatePlayerScore($$5, $$2);
            if ($$6.locked()) {
               $$6.unlock();
               $$4++;
            }
         }

         if ($$4 == 0) {
            throw ERROR_TRIGGER_ALREADY_ENABLED.create();
         } else {
            if ($$1.size() == 1) {
               $$0.sendSuccess(
                  () -> Component.translatable(
                     "commands.scoreboard.players.enable.success.single", new Object[]{$$2.getFormattedDisplayName(), getFirstTargetName($$1)}
                  ),
                  true
               );
            } else {
               $$0.sendSuccess(
                  () -> Component.translatable("commands.scoreboard.players.enable.success.multiple", new Object[]{$$2.getFormattedDisplayName(), $$1.size()}),
                  true
               );
            }

            return $$4;
         }
      }
   }

   private static int resetScores(CommandSourceStack $$0, Collection<ScoreHolder> $$1) {
      Scoreboard $$2 = $$0.getServer().getScoreboard();

      for (ScoreHolder $$3 : $$1) {
         $$2.resetAllPlayerScores($$3);
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.single", new Object[]{getFirstTargetName($$1)}), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.multiple", new Object[]{$$1.size()}), true);
      }

      return $$1.size();
   }

   private static int resetScore(CommandSourceStack $$0, Collection<ScoreHolder> $$1, Objective $$2) {
      Scoreboard $$3 = $$0.getServer().getScoreboard();

      for (ScoreHolder $$4 : $$1) {
         $$3.resetSinglePlayerScore($$4, $$2);
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.reset.specific.single", new Object[]{$$2.getFormattedDisplayName(), getFirstTargetName($$1)}
            ),
            true
         );
      } else {
         $$0.sendSuccess(
            () -> Component.translatable("commands.scoreboard.players.reset.specific.multiple", new Object[]{$$2.getFormattedDisplayName(), $$1.size()}), true
         );
      }

      return $$1.size();
   }

   private static int setScore(CommandSourceStack $$0, Collection<ScoreHolder> $$1, Objective $$2, int $$3) {
      Scoreboard $$4 = $$0.getServer().getScoreboard();

      for (ScoreHolder $$5 : $$1) {
         $$4.getOrCreatePlayerScore($$5, $$2).set($$3);
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.set.success.single", new Object[]{$$2.getFormattedDisplayName(), getFirstTargetName($$1), $$3}
            ),
            true
         );
      } else {
         $$0.sendSuccess(
            () -> Component.translatable("commands.scoreboard.players.set.success.multiple", new Object[]{$$2.getFormattedDisplayName(), $$1.size(), $$3}),
            true
         );
      }

      return $$3 * $$1.size();
   }

   private static int setScoreDisplay(CommandSourceStack $$0, Collection<ScoreHolder> $$1, Objective $$2, @Nullable Component $$3) {
      Scoreboard $$4 = $$0.getServer().getScoreboard();

      for (ScoreHolder $$5 : $$1) {
         $$4.getOrCreatePlayerScore($$5, $$2).display($$3);
      }

      if ($$3 == null) {
         if ($$1.size() == 1) {
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.scoreboard.players.display.name.clear.success.single", new Object[]{getFirstTargetName($$1), $$2.getFormattedDisplayName()}
               ),
               true
            );
         } else {
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.scoreboard.players.display.name.clear.success.multiple", new Object[]{$$1.size(), $$2.getFormattedDisplayName()}
               ),
               true
            );
         }
      } else if ($$1.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.display.name.set.success.single", new Object[]{$$3, getFirstTargetName($$1), $$2.getFormattedDisplayName()}
            ),
            true
         );
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.display.name.set.success.multiple", new Object[]{$$3, $$1.size(), $$2.getFormattedDisplayName()}
            ),
            true
         );
      }

      return $$1.size();
   }

   private static int setScoreNumberFormat(CommandSourceStack $$0, Collection<ScoreHolder> $$1, Objective $$2, @Nullable NumberFormat $$3) {
      Scoreboard $$4 = $$0.getServer().getScoreboard();

      for (ScoreHolder $$5 : $$1) {
         $$4.getOrCreatePlayerScore($$5, $$2).numberFormatOverride($$3);
      }

      if ($$3 == null) {
         if ($$1.size() == 1) {
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.scoreboard.players.display.numberFormat.clear.success.single", new Object[]{getFirstTargetName($$1), $$2.getFormattedDisplayName()}
               ),
               true
            );
         } else {
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.scoreboard.players.display.numberFormat.clear.success.multiple", new Object[]{$$1.size(), $$2.getFormattedDisplayName()}
               ),
               true
            );
         }
      } else if ($$1.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.display.numberFormat.set.success.single", new Object[]{getFirstTargetName($$1), $$2.getFormattedDisplayName()}
            ),
            true
         );
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.display.numberFormat.set.success.multiple", new Object[]{$$1.size(), $$2.getFormattedDisplayName()}
            ),
            true
         );
      }

      return $$1.size();
   }

   private static int addScore(CommandSourceStack $$0, Collection<ScoreHolder> $$1, Objective $$2, int $$3) {
      Scoreboard $$4 = $$0.getServer().getScoreboard();
      int $$5 = 0;

      for (ScoreHolder $$6 : $$1) {
         ScoreAccess $$7 = $$4.getOrCreatePlayerScore($$6, $$2);
         $$7.set($$7.get() + $$3);
         $$5 += $$7.get();
      }

      if ($$1.size() == 1) {
         int $$8 = $$5;
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.add.success.single", new Object[]{$$3, $$2.getFormattedDisplayName(), getFirstTargetName($$1), $$8}
            ),
            true
         );
      } else {
         $$0.sendSuccess(
            () -> Component.translatable("commands.scoreboard.players.add.success.multiple", new Object[]{$$3, $$2.getFormattedDisplayName(), $$1.size()}),
            true
         );
      }

      return $$5;
   }

   private static int removeScore(CommandSourceStack $$0, Collection<ScoreHolder> $$1, Objective $$2, int $$3) {
      Scoreboard $$4 = $$0.getServer().getScoreboard();
      int $$5 = 0;

      for (ScoreHolder $$6 : $$1) {
         ScoreAccess $$7 = $$4.getOrCreatePlayerScore($$6, $$2);
         $$7.set($$7.get() - $$3);
         $$5 += $$7.get();
      }

      if ($$1.size() == 1) {
         int $$8 = $$5;
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.remove.success.single", new Object[]{$$3, $$2.getFormattedDisplayName(), getFirstTargetName($$1), $$8}
            ),
            true
         );
      } else {
         $$0.sendSuccess(
            () -> Component.translatable("commands.scoreboard.players.remove.success.multiple", new Object[]{$$3, $$2.getFormattedDisplayName(), $$1.size()}),
            true
         );
      }

      return $$5;
   }

   private static int listTrackedPlayers(CommandSourceStack $$0) {
      Collection<ScoreHolder> $$1 = $$0.getServer().getScoreboard().getTrackedPlayers();
      if ($$1.isEmpty()) {
         $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.empty"), false);
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.players.list.success", new Object[]{$$1.size(), ComponentUtils.formatList($$1, ScoreHolder::getFeedbackDisplayName)}
            ),
            false
         );
      }

      return $$1.size();
   }

   private static int listTrackedPlayerScores(CommandSourceStack $$0, ScoreHolder $$1) {
      Object2IntMap<Objective> $$2 = $$0.getServer().getScoreboard().listPlayerScores($$1);
      if ($$2.isEmpty()) {
         $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.empty", new Object[]{$$1.getFeedbackDisplayName()}), false);
      } else {
         $$0.sendSuccess(
            () -> Component.translatable("commands.scoreboard.players.list.entity.success", new Object[]{$$1.getFeedbackDisplayName(), $$2.size()}), false
         );
         Object2IntMaps.fastForEach(
            $$2,
            $$1x -> $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.scoreboard.players.list.entity.entry", new Object[]{((Objective)$$1x.getKey()).getFormattedDisplayName(), $$1x.getIntValue()}
               ),
               false
            )
         );
      }

      return $$2.size();
   }

   private static int clearDisplaySlot(CommandSourceStack $$0, DisplaySlot $$1) throws CommandSyntaxException {
      Scoreboard $$2 = $$0.getServer().getScoreboard();
      if ($$2.getDisplayObjective($$1) == null) {
         throw ERROR_DISPLAY_SLOT_ALREADY_EMPTY.create();
      } else {
         $$2.setDisplayObjective($$1, null);
         $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.display.cleared", new Object[]{$$1.getSerializedName()}), true);
         return 0;
      }
   }

   private static int setDisplaySlot(CommandSourceStack $$0, DisplaySlot $$1, Objective $$2) throws CommandSyntaxException {
      Scoreboard $$3 = $$0.getServer().getScoreboard();
      if ($$3.getDisplayObjective($$1) == $$2) {
         throw ERROR_DISPLAY_SLOT_ALREADY_SET.create();
      } else {
         $$3.setDisplayObjective($$1, $$2);
         $$0.sendSuccess(
            () -> Component.translatable("commands.scoreboard.objectives.display.set", new Object[]{$$1.getSerializedName(), $$2.getDisplayName()}), true
         );
         return 0;
      }
   }

   private static int setDisplayName(CommandSourceStack $$0, Objective $$1, Component $$2) {
      if (!$$1.getDisplayName().equals($$2)) {
         $$1.setDisplayName($$2);
         $$0.sendSuccess(
            () -> Component.translatable("commands.scoreboard.objectives.modify.displayname", new Object[]{$$1.getName(), $$1.getFormattedDisplayName()}), true
         );
      }

      return 0;
   }

   private static int setDisplayAutoUpdate(CommandSourceStack $$0, Objective $$1, boolean $$2) {
      if ($$1.displayAutoUpdate() != $$2) {
         $$1.setDisplayAutoUpdate($$2);
         if ($$2) {
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.scoreboard.objectives.modify.displayAutoUpdate.enable", new Object[]{$$1.getName(), $$1.getFormattedDisplayName()}
               ),
               true
            );
         } else {
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.scoreboard.objectives.modify.displayAutoUpdate.disable", new Object[]{$$1.getName(), $$1.getFormattedDisplayName()}
               ),
               true
            );
         }
      }

      return 0;
   }

   private static int setObjectiveFormat(CommandSourceStack $$0, Objective $$1, @Nullable NumberFormat $$2) {
      $$1.setNumberFormat($$2);
      if ($$2 != null) {
         $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.objectiveFormat.set", new Object[]{$$1.getName()}), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.objectiveFormat.clear", new Object[]{$$1.getName()}), true);
      }

      return 0;
   }

   private static int setRenderType(CommandSourceStack $$0, Objective $$1, RenderType $$2) {
      if ($$1.getRenderType() != $$2) {
         $$1.setRenderType($$2);
         $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.rendertype", new Object[]{$$1.getFormattedDisplayName()}), true);
      }

      return 0;
   }

   private static int removeObjective(CommandSourceStack $$0, Objective $$1) {
      Scoreboard $$2 = $$0.getServer().getScoreboard();
      $$2.removeObjective($$1);
      $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.remove.success", new Object[]{$$1.getFormattedDisplayName()}), true);
      return $$2.getObjectives().size();
   }

   private static int addObjective(CommandSourceStack $$0, String $$1, ObjectiveCriteria $$2, Component $$3) throws CommandSyntaxException {
      Scoreboard $$4 = $$0.getServer().getScoreboard();
      if ($$4.getObjective($$1) != null) {
         throw ERROR_OBJECTIVE_ALREADY_EXISTS.create();
      } else {
         $$4.addObjective($$1, $$2, $$3, $$2.getDefaultRenderType(), false, null);
         Objective $$5 = $$4.getObjective($$1);
         $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.add.success", new Object[]{$$5.getFormattedDisplayName()}), true);
         return $$4.getObjectives().size();
      }
   }

   private static int listObjectives(CommandSourceStack $$0) {
      Collection<Objective> $$1 = $$0.getServer().getScoreboard().getObjectives();
      if ($$1.isEmpty()) {
         $$0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.list.empty"), false);
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.scoreboard.objectives.list.success", new Object[]{$$1.size(), ComponentUtils.formatList($$1, Objective::getFormattedDisplayName)}
            ),
            false
         );
      }

      return $$1.size();
   }

   @FunctionalInterface
   public interface NumberFormatCommandExecutor {
      int run(CommandContext<CommandSourceStack> var1, @Nullable NumberFormat var2) throws CommandSyntaxException;
   }
}
