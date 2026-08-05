/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface CommandArgumentParser<T> {
    public T parseForCommands(StringReader var1) throws CommandSyntaxException;

    public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder var1);

    default public <S> CommandArgumentParser<S> mapResult(final Function<T, S> $$0) {
        return new CommandArgumentParser<S>(){

            @Override
            public S parseForCommands(StringReader $$02) throws CommandSyntaxException {
                return $$0.apply(CommandArgumentParser.this.parseForCommands($$02));
            }

            @Override
            public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder $$02) {
                return CommandArgumentParser.this.parseForSuggestions($$02);
            }
        };
    }

    default public <T, O> CommandArgumentParser<T> withCodec(final DynamicOps<O> $$0, final CommandArgumentParser<O> $$1, final Codec<T> $$2, final DynamicCommandExceptionType $$3) {
        return new CommandArgumentParser<T>(){

            @Override
            public T parseForCommands(StringReader $$02) throws CommandSyntaxException {
                int $$12 = $$02.getCursor();
                Object $$22 = $$1.parseForCommands($$02);
                DataResult $$32 = $$2.parse($$0, $$22);
                return $$32.getOrThrow($$3 -> {
                    $$02.setCursor($$12);
                    return $$3.createWithContext((ImmutableStringReader)$$02, $$3);
                });
            }

            @Override
            public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder $$02) {
                return CommandArgumentParser.this.parseForSuggestions($$02);
            }
        };
    }
}

