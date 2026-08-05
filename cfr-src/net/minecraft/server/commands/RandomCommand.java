/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomSequences;
import org.jspecify.annotations.Nullable;

public class RandomCommand {
    private static final SimpleCommandExceptionType ERROR_RANGE_TOO_LARGE = new SimpleCommandExceptionType((Message)Component.translatable("commands.random.error.range_too_large"));
    private static final SimpleCommandExceptionType ERROR_RANGE_TOO_SMALL = new SimpleCommandExceptionType((Message)Component.translatable("commands.random.error.range_too_small"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("random").then(RandomCommand.drawRandomValueTree("value", false))).then(RandomCommand.drawRandomValueTree("roll", true))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("reset").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((LiteralArgumentBuilder)Commands.literal("*").executes($$0 -> RandomCommand.resetAllSequences((CommandSourceStack)$$0.getSource()))).then(((RequiredArgumentBuilder)Commands.argument("seed", IntegerArgumentType.integer()).executes($$0 -> RandomCommand.resetAllSequencesAndSetNewDefaults((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"seed"), true, true))).then(((RequiredArgumentBuilder)Commands.argument("includeWorldSeed", BoolArgumentType.bool()).executes($$0 -> RandomCommand.resetAllSequencesAndSetNewDefaults((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"seed"), BoolArgumentType.getBool((CommandContext)$$0, (String)"includeWorldSeed"), true))).then(Commands.argument("includeSequenceId", BoolArgumentType.bool()).executes($$0 -> RandomCommand.resetAllSequencesAndSetNewDefaults((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"seed"), BoolArgumentType.getBool((CommandContext)$$0, (String)"includeWorldSeed"), BoolArgumentType.getBool((CommandContext)$$0, (String)"includeSequenceId")))))))).then(((RequiredArgumentBuilder)Commands.argument("sequence", IdentifierArgument.id()).suggests(RandomCommand::suggestRandomSequence).executes($$0 -> RandomCommand.resetSequence((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "sequence")))).then(((RequiredArgumentBuilder)Commands.argument("seed", IntegerArgumentType.integer()).executes($$0 -> RandomCommand.resetSequence((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "sequence"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"seed"), true, true))).then(((RequiredArgumentBuilder)Commands.argument("includeWorldSeed", BoolArgumentType.bool()).executes($$0 -> RandomCommand.resetSequence((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "sequence"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"seed"), BoolArgumentType.getBool((CommandContext)$$0, (String)"includeWorldSeed"), true))).then(Commands.argument("includeSequenceId", BoolArgumentType.bool()).executes($$0 -> RandomCommand.resetSequence((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "sequence"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"seed"), BoolArgumentType.getBool((CommandContext)$$0, (String)"includeWorldSeed"), BoolArgumentType.getBool((CommandContext)$$0, (String)"includeSequenceId")))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> drawRandomValueTree(String $$0, boolean $$12) {
        return (LiteralArgumentBuilder)Commands.literal($$0).then(((RequiredArgumentBuilder)Commands.argument("range", RangeArgument.intRange()).executes($$1 -> RandomCommand.randomSample((CommandSourceStack)$$1.getSource(), RangeArgument.Ints.getRange((CommandContext<CommandSourceStack>)$$1, "range"), null, $$12))).then(((RequiredArgumentBuilder)Commands.argument("sequence", IdentifierArgument.id()).suggests(RandomCommand::suggestRandomSequence).requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes($$1 -> RandomCommand.randomSample((CommandSourceStack)$$1.getSource(), RangeArgument.Ints.getRange((CommandContext<CommandSourceStack>)$$1, "range"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$1, "sequence"), $$12))));
    }

    private static CompletableFuture<Suggestions> suggestRandomSequence(CommandContext<CommandSourceStack> $$0, SuggestionsBuilder $$12) {
        ArrayList $$22 = Lists.newArrayList();
        ((CommandSourceStack)$$0.getSource()).getLevel().getRandomSequences().forAllSequences(($$1, $$2) -> $$22.add($$1.toString()));
        return SharedSuggestionProvider.suggest($$22, $$12);
    }

    private static int randomSample(CommandSourceStack $$0, MinMaxBounds.Ints $$1, @Nullable Identifier $$2, boolean $$3) throws CommandSyntaxException {
        RandomSource $$5;
        if ($$2 != null) {
            RandomSource $$4 = $$0.getLevel().getRandomSequence($$2);
        } else {
            $$5 = $$0.getLevel().getRandom();
        }
        int $$6 = $$1.min().orElse(Integer.MIN_VALUE);
        int $$7 = $$1.max().orElse(Integer.MAX_VALUE);
        long $$8 = (long)$$7 - (long)$$6;
        if ($$8 == 0L) {
            throw ERROR_RANGE_TOO_SMALL.create();
        }
        if ($$8 >= Integer.MAX_VALUE) {
            throw ERROR_RANGE_TOO_LARGE.create();
        }
        int $$9 = Mth.randomBetweenInclusive($$5, $$6, $$7);
        if ($$3) {
            $$0.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("commands.random.roll", $$0.getDisplayName(), $$9, $$6, $$7), false);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.random.sample.success", $$9), false);
        }
        return $$9;
    }

    private static int resetSequence(CommandSourceStack $$0, Identifier $$1) throws CommandSyntaxException {
        ServerLevel $$2 = $$0.getLevel();
        $$2.getRandomSequences().reset($$1, $$2.getSeed());
        $$0.sendSuccess(() -> Component.translatable("commands.random.reset.success", Component.translationArg($$1)), false);
        return 1;
    }

    private static int resetSequence(CommandSourceStack $$0, Identifier $$1, int $$2, boolean $$3, boolean $$4) throws CommandSyntaxException {
        ServerLevel $$5 = $$0.getLevel();
        $$5.getRandomSequences().reset($$1, $$5.getSeed(), $$2, $$3, $$4);
        $$0.sendSuccess(() -> Component.translatable("commands.random.reset.success", Component.translationArg($$1)), false);
        return 1;
    }

    private static int resetAllSequences(CommandSourceStack $$0) {
        int $$1 = $$0.getLevel().getRandomSequences().clear();
        $$0.sendSuccess(() -> Component.translatable("commands.random.reset.all.success", $$1), false);
        return $$1;
    }

    private static int resetAllSequencesAndSetNewDefaults(CommandSourceStack $$0, int $$1, boolean $$2, boolean $$3) {
        RandomSequences $$4 = $$0.getLevel().getRandomSequences();
        $$4.setSeedDefaults($$1, $$2, $$3);
        int $$5 = $$4.clear();
        $$0.sendSuccess(() -> Component.translatable("commands.random.reset.all.success", $$5), false);
        return $$5;
    }
}

