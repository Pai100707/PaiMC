package net.minecraft.commands.arguments;

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
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

public class GameModeArgument implements ArgumentType<GameType> {
   private static final Collection<String> EXAMPLES = Stream.of(GameType.SURVIVAL, GameType.CREATIVE)
      .<String>map(GameType::getName)
      .collect(Collectors.toList());
   private static final GameType[] VALUES = GameType.values();
   private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.gamemode.invalid", new Object[]{$$0})
   );

   public GameType parse(StringReader $$0) throws CommandSyntaxException {
      String $$1 = $$0.readUnquotedString();
      GameType $$2 = GameType.byName($$1, null);
      if ($$2 == null) {
         throw ERROR_INVALID.createWithContext($$0, $$1);
      } else {
         return $$2;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return $$0.getSource() instanceof net.minecraft.commands.SharedSuggestionProvider
         ? net.minecraft.commands.SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(GameType::getName), $$1)
         : Suggestions.empty();
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static GameModeArgument gameMode() {
      return new GameModeArgument();
   }

   public static GameType getGameMode(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return (GameType)$$0.getArgument($$1, GameType.class);
   }
}
