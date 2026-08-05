/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class TriggerCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType((Message)Component.translatable("commands.trigger.failed.unprimed"));
    private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType((Message)Component.translatable("commands.trigger.failed.invalid"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)Commands.literal("trigger").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective()).suggests(($$0, $$1) -> TriggerCommand.suggestObjectives((CommandSourceStack)$$0.getSource(), $$1)).executes($$0 -> TriggerCommand.simpleTrigger((CommandSourceStack)$$0.getSource(), ((CommandSourceStack)$$0.getSource()).getPlayerOrException(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)$$0, "objective")))).then(Commands.literal("add").then(Commands.argument("value", IntegerArgumentType.integer()).executes($$0 -> TriggerCommand.addValue((CommandSourceStack)$$0.getSource(), ((CommandSourceStack)$$0.getSource()).getPlayerOrException(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)$$0, "objective"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"value")))))).then(Commands.literal("set").then(Commands.argument("value", IntegerArgumentType.integer()).executes($$0 -> TriggerCommand.setValue((CommandSourceStack)$$0.getSource(), ((CommandSourceStack)$$0.getSource()).getPlayerOrException(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)$$0, "objective"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"value")))))));
    }

    public static CompletableFuture<Suggestions> suggestObjectives(CommandSourceStack $$0, SuggestionsBuilder $$1) {
        Entity $$2 = $$0.getEntity();
        ArrayList $$3 = Lists.newArrayList();
        if ($$2 != null) {
            ServerScoreboard $$4 = $$0.getServer().getScoreboard();
            for (Objective $$5 : $$4.getObjectives()) {
                ReadOnlyScoreInfo $$6;
                if ($$5.getCriteria() != ObjectiveCriteria.TRIGGER || ($$6 = $$4.getPlayerScoreInfo($$2, $$5)) == null || $$6.isLocked()) continue;
                $$3.add($$5.getName());
            }
        }
        return SharedSuggestionProvider.suggest($$3, $$1);
    }

    private static int addValue(CommandSourceStack $$0, ServerPlayer $$1, Objective $$2, int $$3) throws CommandSyntaxException {
        ScoreAccess $$4 = TriggerCommand.getScore($$0.getServer().getScoreboard(), $$1, $$2);
        int $$5 = $$4.add($$3);
        $$0.sendSuccess(() -> Component.translatable("commands.trigger.add.success", $$2.getFormattedDisplayName(), $$3), true);
        return $$5;
    }

    private static int setValue(CommandSourceStack $$0, ServerPlayer $$1, Objective $$2, int $$3) throws CommandSyntaxException {
        ScoreAccess $$4 = TriggerCommand.getScore($$0.getServer().getScoreboard(), $$1, $$2);
        $$4.set($$3);
        $$0.sendSuccess(() -> Component.translatable("commands.trigger.set.success", $$2.getFormattedDisplayName(), $$3), true);
        return $$3;
    }

    private static int simpleTrigger(CommandSourceStack $$0, ServerPlayer $$1, Objective $$2) throws CommandSyntaxException {
        ScoreAccess $$3 = TriggerCommand.getScore($$0.getServer().getScoreboard(), $$1, $$2);
        int $$4 = $$3.add(1);
        $$0.sendSuccess(() -> Component.translatable("commands.trigger.simple.success", $$2.getFormattedDisplayName()), true);
        return $$4;
    }

    private static ScoreAccess getScore(Scoreboard $$0, ScoreHolder $$1, Objective $$2) throws CommandSyntaxException {
        if ($$2.getCriteria() != ObjectiveCriteria.TRIGGER) {
            throw ERROR_INVALID_OBJECTIVE.create();
        }
        ReadOnlyScoreInfo $$3 = $$0.getPlayerScoreInfo($$1, $$2);
        if ($$3 == null || $$3.isLocked()) {
            throw ERROR_NOT_PRIMED.create();
        }
        ScoreAccess $$4 = $$0.getOrCreatePlayerScore($$1, $$2);
        $$4.lock();
        return $$4;
    }
}

