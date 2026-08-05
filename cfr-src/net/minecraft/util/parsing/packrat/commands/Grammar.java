/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.ErrorEntry;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.util.parsing.packrat.commands.ResourceSuggestion;
import net.minecraft.util.parsing.packrat.commands.StringReaderParserState;

public record Grammar<T>(Dictionary<StringReader> rules, NamedRule<StringReader, T> top) implements CommandArgumentParser<T>
{
    public Grammar {
        $$0.checkAllBound();
    }

    public Optional<T> parse(ParseState<StringReader> $$0) {
        return $$0.parseTopRule(this.top);
    }

    @Override
    public T parseForCommands(StringReader $$0) throws CommandSyntaxException {
        Object r;
        ErrorCollector.LongestOnly<StringReader> $$12 = new ErrorCollector.LongestOnly<StringReader>();
        StringReaderParserState $$22 = new StringReaderParserState($$12, $$0);
        Optional<T> $$3 = this.parse($$22);
        if ($$3.isPresent()) {
            return $$3.get();
        }
        List<ErrorEntry<StringReader>> $$4 = $$12.entries();
        List $$5 = $$4.stream().mapMulti(($$1, $$2) -> {
            Object $$3 = $$1.reason();
            if ($$3 instanceof DelayedException) {
                DelayedException $$4 = (DelayedException)$$3;
                $$2.accept($$4.create($$0.getString(), $$1.cursor()));
            } else {
                Object $$5 = $$1.reason();
                if ($$5 instanceof Exception) {
                    Exception $$6 = (Exception)$$5;
                    $$2.accept($$6);
                }
            }
        }).toList();
        for (Exception $$6 : $$5) {
            if (!($$6 instanceof CommandSyntaxException)) continue;
            CommandSyntaxException $$7 = (CommandSyntaxException)((Object)$$6);
            throw $$7;
        }
        if ($$5.size() == 1 && (r = $$5.get(0)) instanceof RuntimeException) {
            RuntimeException $$8 = (RuntimeException)r;
            throw $$8;
        }
        throw new IllegalStateException("Failed to parse: " + $$4.stream().map(ErrorEntry::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder $$0) {
        StringReader $$1 = new StringReader($$0.getInput());
        $$1.setCursor($$0.getStart());
        ErrorCollector.LongestOnly<StringReader> $$2 = new ErrorCollector.LongestOnly<StringReader>();
        StringReaderParserState $$3 = new StringReaderParserState($$2, $$1);
        this.parse($$3);
        List<ErrorEntry<StringReader>> $$4 = $$2.entries();
        if ($$4.isEmpty()) {
            return $$0.buildFuture();
        }
        SuggestionsBuilder $$5 = $$0.createOffset($$2.cursor());
        for (ErrorEntry<StringReader> $$6 : $$4) {
            SuggestionSupplier<StringReader> suggestionSupplier = $$6.suggestions();
            if (suggestionSupplier instanceof ResourceSuggestion) {
                ResourceSuggestion $$7 = (ResourceSuggestion)suggestionSupplier;
                SharedSuggestionProvider.suggestResource($$7.possibleResources(), $$5);
                continue;
            }
            SharedSuggestionProvider.suggest($$6.suggestions().possibleValues($$3), $$5);
        }
        return $$5.buildFuture();
    }
}

