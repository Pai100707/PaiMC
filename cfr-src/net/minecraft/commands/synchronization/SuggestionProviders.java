/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.synchronization;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class SuggestionProviders {
    private static final Map<Identifier, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME = new HashMap<Identifier, SuggestionProvider<SharedSuggestionProvider>>();
    private static final Identifier ID_ASK_SERVER = Identifier.withDefaultNamespace("ask_server");
    public static final SuggestionProvider<SharedSuggestionProvider> ASK_SERVER = SuggestionProviders.register(ID_ASK_SERVER, (SuggestionProvider<SharedSuggestionProvider>)((SuggestionProvider)($$0, $$1) -> ((SharedSuggestionProvider)$$0.getSource()).customSuggestion($$0)));
    public static final SuggestionProvider<SharedSuggestionProvider> AVAILABLE_SOUNDS = SuggestionProviders.register(Identifier.withDefaultNamespace("available_sounds"), (SuggestionProvider<SharedSuggestionProvider>)((SuggestionProvider)($$0, $$1) -> SharedSuggestionProvider.suggestResource(((SharedSuggestionProvider)$$0.getSource()).getAvailableSounds(), $$1)));
    public static final SuggestionProvider<SharedSuggestionProvider> SUMMONABLE_ENTITIES = SuggestionProviders.register(Identifier.withDefaultNamespace("summonable_entities"), (SuggestionProvider<SharedSuggestionProvider>)((SuggestionProvider)($$0, $$12) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.stream().filter($$1 -> $$1.isEnabled(((SharedSuggestionProvider)$$0.getSource()).enabledFeatures()) && $$1.canSummon()), $$12, EntityType::getKey, EntityType::getDescription)));

    public static <S extends SharedSuggestionProvider> SuggestionProvider<S> register(Identifier $$0, SuggestionProvider<SharedSuggestionProvider> $$1) {
        SuggestionProvider<SharedSuggestionProvider> $$2 = PROVIDERS_BY_NAME.putIfAbsent($$0, $$1);
        if ($$2 != null) {
            throw new IllegalArgumentException("A command suggestion provider is already registered with the name '" + String.valueOf($$0) + "'");
        }
        return new RegisteredSuggestion($$0, $$1);
    }

    public static <S extends SharedSuggestionProvider> SuggestionProvider<S> cast(SuggestionProvider<SharedSuggestionProvider> $$0) {
        return $$0;
    }

    public static <S extends SharedSuggestionProvider> SuggestionProvider<S> getProvider(Identifier $$0) {
        return SuggestionProviders.cast(PROVIDERS_BY_NAME.getOrDefault($$0, ASK_SERVER));
    }

    public static Identifier getName(SuggestionProvider<?> $$0) {
        Identifier identifier;
        if ($$0 instanceof RegisteredSuggestion) {
            RegisteredSuggestion $$1 = (RegisteredSuggestion)$$0;
            identifier = $$1.name;
        } else {
            identifier = ID_ASK_SERVER;
        }
        return identifier;
    }

    static final class RegisteredSuggestion
    extends Record
    implements SuggestionProvider<SharedSuggestionProvider> {
        final Identifier name;
        private final SuggestionProvider<SharedSuggestionProvider> delegate;

        RegisteredSuggestion(Identifier $$0, SuggestionProvider<SharedSuggestionProvider> $$1) {
            this.name = $$0;
            this.delegate = $$1;
        }

        public CompletableFuture<Suggestions> getSuggestions(CommandContext<SharedSuggestionProvider> $$0, SuggestionsBuilder $$1) throws CommandSyntaxException {
            return this.delegate.getSuggestions($$0, $$1);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegisteredSuggestion.class, "name;delegate", "name", "delegate"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegisteredSuggestion.class, "name;delegate", "name", "delegate"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegisteredSuggestion.class, "name;delegate", "name", "delegate"}, this, $$0);
        }

        public Identifier name() {
            return this.name;
        }

        public SuggestionProvider<SharedSuggestionProvider> delegate() {
            return this.delegate;
        }
    }
}

