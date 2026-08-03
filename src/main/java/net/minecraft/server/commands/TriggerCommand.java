package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class TriggerCommand {
   private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType(Component.translatable("commands.trigger.failed.unprimed"));
   private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType(
      Component.translatable("commands.trigger.failed.invalid")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)Commands.literal("trigger")
            .then(
               ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective())
                        .suggests(($$0x, $$1) -> suggestObjectives((CommandSourceStack)$$0x.getSource(), $$1))
                        .executes(
                           $$0x -> simpleTrigger(
                              (CommandSourceStack)$$0x.getSource(),
                              ((CommandSourceStack)$$0x.getSource()).getPlayerOrException(),
                              ObjectiveArgument.getObjective($$0x, "objective")
                           )
                        ))
                     .then(
                        Commands.literal("add")
                           .then(
                              Commands.argument("value", IntegerArgumentType.integer())
                                 .executes(
                                    $$0x -> addValue(
                                       (CommandSourceStack)$$0x.getSource(),
                                       ((CommandSourceStack)$$0x.getSource()).getPlayerOrException(),
                                       ObjectiveArgument.getObjective($$0x, "objective"),
                                       IntegerArgumentType.getInteger($$0x, "value")
                                    )
                                 )
                           )
                     ))
                  .then(
                     Commands.literal("set")
                        .then(
                           Commands.argument("value", IntegerArgumentType.integer())
                              .executes(
                                 $$0x -> setValue(
                                    (CommandSourceStack)$$0x.getSource(),
                                    ((CommandSourceStack)$$0x.getSource()).getPlayerOrException(),
                                    ObjectiveArgument.getObjective($$0x, "objective"),
                                    IntegerArgumentType.getInteger($$0x, "value")
                                 )
                              )
                        )
                  )
            )
      );
   }

   public static CompletableFuture<Suggestions> suggestObjectives(CommandSourceStack $$0, SuggestionsBuilder $$1) {
      ScoreHolder $$2 = $$0.getEntity();
      List<String> $$3 = Lists.newArrayList();
      if ($$2 != null) {
         Scoreboard $$4 = $$0.getServer().getScoreboard();

         for (Objective $$5 : $$4.getObjectives()) {
            if ($$5.getCriteria() == ObjectiveCriteria.TRIGGER) {
               ReadOnlyScoreInfo $$6 = $$4.getPlayerScoreInfo($$2, $$5);
               if ($$6 != null && !$$6.isLocked()) {
                  $$3.add($$5.getName());
               }
            }
         }
      }

      return SharedSuggestionProvider.suggest($$3, $$1);
   }

   private static int addValue(CommandSourceStack $$0, ServerPlayer $$1, Objective $$2, int $$3) throws CommandSyntaxException {
      ScoreAccess $$4 = getScore($$0.getServer().getScoreboard(), $$1, $$2);
      int $$5 = $$4.add($$3);
      $$0.sendSuccess(() -> Component.translatable("commands.trigger.add.success", new Object[]{$$2.getFormattedDisplayName(), $$3}), true);
      return $$5;
   }

   private static int setValue(CommandSourceStack $$0, ServerPlayer $$1, Objective $$2, int $$3) throws CommandSyntaxException {
      ScoreAccess $$4 = getScore($$0.getServer().getScoreboard(), $$1, $$2);
      $$4.set($$3);
      $$0.sendSuccess(() -> Component.translatable("commands.trigger.set.success", new Object[]{$$2.getFormattedDisplayName(), $$3}), true);
      return $$3;
   }

   private static int simpleTrigger(CommandSourceStack $$0, ServerPlayer $$1, Objective $$2) throws CommandSyntaxException {
      ScoreAccess $$3 = getScore($$0.getServer().getScoreboard(), $$1, $$2);
      int $$4 = $$3.add(1);
      $$0.sendSuccess(() -> Component.translatable("commands.trigger.simple.success", new Object[]{$$2.getFormattedDisplayName()}), true);
      return $$4;
   }

   private static ScoreAccess getScore(Scoreboard $$0, ScoreHolder $$1, Objective $$2) throws CommandSyntaxException {
      if ($$2.getCriteria() != ObjectiveCriteria.TRIGGER) {
         throw ERROR_INVALID_OBJECTIVE.create();
      } else {
         ReadOnlyScoreInfo $$3 = $$0.getPlayerScoreInfo($$1, $$2);
         if ($$3 != null && !$$3.isLocked()) {
            ScoreAccess $$4 = $$0.getOrCreatePlayerScore($$1, $$2);
            $$4.lock();
            return $$4;
         } else {
            throw ERROR_NOT_PRIMED.create();
         }
      }
   }
}
