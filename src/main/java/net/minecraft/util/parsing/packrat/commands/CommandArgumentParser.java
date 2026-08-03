package net.minecraft.util.parsing.packrat.commands;

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
   T parseForCommands(StringReader var1) throws CommandSyntaxException;

   CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder var1);

   default <S> CommandArgumentParser<S> mapResult(final Function<T, S> $$0) {
      return new CommandArgumentParser<S>() {
         @Override
         public S parseForCommands(StringReader $$0x) throws CommandSyntaxException {
            return $$0.apply((T)CommandArgumentParser.this.parseForCommands($$0));
         }

         @Override
         public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder $$0x) {
            return CommandArgumentParser.this.parseForSuggestions($$0);
         }
      };
   }

   default <T, O> CommandArgumentParser<T> withCodec(
      final DynamicOps<O> $$0, final CommandArgumentParser<O> $$1, final Codec<T> $$2, final DynamicCommandExceptionType $$3
   ) {
      return new CommandArgumentParser<T>() {
         @Override
         public T parseForCommands(StringReader $$0x) throws CommandSyntaxException {
            int $$1x = $$0.getCursor();
            O $$2x = $$1.parseForCommands($$0);
            DataResult<T> $$3x = $$2.parse($$0, $$2x);
            return (T)$$3x.getOrThrow($$3xxx -> {
               $$0.setCursor($$1);
               return $$3.createWithContext($$0, $$3xxx);
            });
         }

         @Override
         public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder $$0x) {
            return CommandArgumentParser.this.parseForSuggestions($$0);
         }
      };
   }
}
