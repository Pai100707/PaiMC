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

public record Grammar<T>(Dictionary<StringReader> rules, NamedRule<StringReader, T> top) implements CommandArgumentParser<T> {
   public Grammar(Dictionary<StringReader> rules, NamedRule<StringReader, T> top) {
      rules.checkAllBound();
      this.rules = rules;
      this.top = top;
   }

   public Optional<T> parse(ParseState<StringReader> $$0) {
      return $$0.parseTopRule(this.top);
   }

   @Override
   public T parseForCommands(StringReader $$0) throws CommandSyntaxException {
      ErrorCollector.LongestOnly<StringReader> $$1 = new ErrorCollector.LongestOnly<>();
      StringReaderParserState $$2 = new StringReaderParserState($$1, $$0);
      Optional<T> $$3 = this.parse($$2);
      if ($$3.isPresent()) {
         return $$3.get();
      } else {
         List<ErrorEntry<StringReader>> $$4 = $$1.entries();
         List<Exception> $$5 = $$4.stream().<Exception>mapMulti(($$1x, $$2x) -> {
            if ($$1x.reason() instanceof DelayedException<?> $$4x) {
               $$2x.accept($$4x.create($$0.getString(), $$1x.cursor()));
            } else if ($$1x.reason() instanceof Exception $$6x) {
               $$2x.accept($$6x);
            }
         }).toList();

         for (Exception $$6 : $$5) {
            if ($$6 instanceof CommandSyntaxException $$7) {
               throw $$7;
            }
         }

         if ($$5.size() == 1 && $$5.get(0) instanceof RuntimeException $$8) {
            throw $$8;
         } else {
            throw new IllegalStateException("Failed to parse: " + $$4.stream().map(ErrorEntry::toString).collect(Collectors.joining(", ")));
         }
      }
   }

   @Override
   public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder $$0) {
      StringReader $$1 = new StringReader($$0.getInput());
      $$1.setCursor($$0.getStart());
      ErrorCollector.LongestOnly<StringReader> $$2 = new ErrorCollector.LongestOnly<>();
      StringReaderParserState $$3 = new StringReaderParserState($$2, $$1);
      this.parse($$3);
      List<ErrorEntry<StringReader>> $$4 = $$2.entries();
      if ($$4.isEmpty()) {
         return $$0.buildFuture();
      } else {
         SuggestionsBuilder $$5 = $$0.createOffset($$2.cursor());

         for (ErrorEntry<StringReader> $$6 : $$4) {
            if ($$6.suggestions() instanceof ResourceSuggestion $$7) {
               SharedSuggestionProvider.suggestResource($$7.possibleResources(), $$5);
            } else {
               SharedSuggestionProvider.suggest($$6.suggestions().possibleValues($$3), $$5);
            }
         }

         return $$5.buildFuture();
      }
   }
}
