package net.minecraft.commands.synchronization;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class SuggestionProviders {
   private static final Map<Identifier, SuggestionProvider<net.minecraft.commands.SharedSuggestionProvider>> PROVIDERS_BY_NAME = new HashMap<>();
   private static final Identifier ID_ASK_SERVER = Identifier.withDefaultNamespace("ask_server");
   public static final SuggestionProvider<net.minecraft.commands.SharedSuggestionProvider> ASK_SERVER = register(
      ID_ASK_SERVER, ($$0, $$1) -> ((net.minecraft.commands.SharedSuggestionProvider)$$0.getSource()).customSuggestion($$0)
   );
   public static final SuggestionProvider<net.minecraft.commands.SharedSuggestionProvider> AVAILABLE_SOUNDS = register(
      Identifier.withDefaultNamespace("available_sounds"),
      ($$0, $$1) -> net.minecraft.commands.SharedSuggestionProvider.suggestResource(
         ((net.minecraft.commands.SharedSuggestionProvider)$$0.getSource()).getAvailableSounds(), $$1
      )
   );
   public static final SuggestionProvider<net.minecraft.commands.SharedSuggestionProvider> SUMMONABLE_ENTITIES = register(
      Identifier.withDefaultNamespace("summonable_entities"),
      ($$0, $$1) -> net.minecraft.commands.SharedSuggestionProvider.suggestResource(
         BuiltInRegistries.ENTITY_TYPE
            .stream()
            .filter($$1x -> $$1x.isEnabled(((net.minecraft.commands.SharedSuggestionProvider)$$0.getSource()).enabledFeatures()) && $$1x.canSummon()),
         $$1,
         EntityType::getKey,
         EntityType::getDescription
      )
   );

   public static <S extends net.minecraft.commands.SharedSuggestionProvider> SuggestionProvider<S> register(
      Identifier $$0, SuggestionProvider<net.minecraft.commands.SharedSuggestionProvider> $$1
   ) {
      SuggestionProvider<net.minecraft.commands.SharedSuggestionProvider> $$2 = PROVIDERS_BY_NAME.putIfAbsent($$0, $$1);
      if ($$2 != null) {
         throw new IllegalArgumentException("A command suggestion provider is already registered with the name '" + $$0 + "'");
      } else {
         return new SuggestionProviders.RegisteredSuggestion($$0, $$1);
      }
   }

   public static <S extends net.minecraft.commands.SharedSuggestionProvider> SuggestionProvider<S> cast(
      SuggestionProvider<net.minecraft.commands.SharedSuggestionProvider> $$0
   ) {
      return (SuggestionProvider<S>)$$0;
   }

   public static <S extends net.minecraft.commands.SharedSuggestionProvider> SuggestionProvider<S> getProvider(Identifier $$0) {
      return cast(PROVIDERS_BY_NAME.getOrDefault($$0, ASK_SERVER));
   }

   public static Identifier getName(SuggestionProvider<?> $$0) {
      return $$0 instanceof SuggestionProviders.RegisteredSuggestion $$1 ? $$1.name : ID_ASK_SERVER;
   }

   record RegisteredSuggestion(Identifier name, SuggestionProvider<net.minecraft.commands.SharedSuggestionProvider> delegate)
      implements SuggestionProvider<net.minecraft.commands.SharedSuggestionProvider> {

      public CompletableFuture<Suggestions> getSuggestions(CommandContext<net.minecraft.commands.SharedSuggestionProvider> $$0, SuggestionsBuilder $$1) throws CommandSyntaxException {
         return this.delegate.getSuggestions($$0, $$1);
      }
   }
}
