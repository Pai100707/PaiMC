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
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class TeamArgument implements ArgumentType<String> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "123");
   private static final DynamicCommandExceptionType ERROR_TEAM_NOT_FOUND = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("team.notFound", new Object[]{$$0})
   );

   public static TeamArgument team() {
      return new TeamArgument();
   }

   public static PlayerTeam getTeam(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      String $$2 = (String)$$0.getArgument($$1, String.class);
      Scoreboard $$3 = ((net.minecraft.commands.CommandSourceStack)$$0.getSource()).getServer().getScoreboard();
      PlayerTeam $$4 = $$3.getPlayerTeam($$2);
      if ($$4 == null) {
         throw ERROR_TEAM_NOT_FOUND.create($$2);
      } else {
         return $$4;
      }
   }

   public String parse(StringReader $$0) throws CommandSyntaxException {
      return $$0.readUnquotedString();
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return $$0.getSource() instanceof net.minecraft.commands.SharedSuggestionProvider
         ? net.minecraft.commands.SharedSuggestionProvider.suggest(((net.minecraft.commands.SharedSuggestionProvider)$$0.getSource()).getAllTeams(), $$1)
         : Suggestions.empty();
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
