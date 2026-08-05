/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

public class GameModeArgument
implements ArgumentType<GameType> {
    private static final Collection<String> EXAMPLES = Stream.of(GameType.SURVIVAL, GameType.CREATIVE).map(GameType::getName).collect(Collectors.toList());
    private static final GameType[] VALUES = GameType.values();
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.gamemode.invalid", $$0));

    public GameType parse(StringReader $$0) throws CommandSyntaxException {
        String $$1 = $$0.readUnquotedString();
        GameType $$2 = GameType.byName($$1, null);
        if ($$2 == null) {
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)$$0, (Object)$$1);
        }
        return $$2;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
        if ($$0.getSource() instanceof SharedSuggestionProvider) {
            return SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(GameType::getName), $$1);
        }
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static GameModeArgument gameMode() {
        return new GameModeArgument();
    }

    public static GameType getGameMode(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return (GameType)$$0.getArgument($$1, GameType.class);
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

