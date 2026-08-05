/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.CharMatcher
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.permissions.PermissionSetSupplier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;

public interface SharedSuggestionProvider
extends PermissionSetSupplier {
    public static final CharMatcher MATCH_SPLITTER = CharMatcher.anyOf((CharSequence)"._/");

    public Collection<String> getOnlinePlayerNames();

    default public Collection<String> getCustomTabSugggestions() {
        return this.getOnlinePlayerNames();
    }

    default public Collection<String> getSelectedEntities() {
        return Collections.emptyList();
    }

    public Collection<String> getAllTeams();

    public Stream<Identifier> getAvailableSounds();

    public CompletableFuture<Suggestions> customSuggestion(CommandContext<?> var1);

    default public Collection<TextCoordinates> getRelevantCoordinates() {
        return Collections.singleton(TextCoordinates.DEFAULT_GLOBAL);
    }

    default public Collection<TextCoordinates> getAbsoluteCoordinates() {
        return Collections.singleton(TextCoordinates.DEFAULT_GLOBAL);
    }

    public Set<ResourceKey<Level>> levels();

    public RegistryAccess registryAccess();

    public FeatureFlagSet enabledFeatures();

    default public void suggestRegistryElements(HolderLookup<?> $$0, ElementSuggestionType $$1, SuggestionsBuilder $$2) {
        if ($$1.shouldSuggestTags()) {
            SharedSuggestionProvider.suggestResource($$0.listTagIds().map(TagKey::location), $$2, "#");
        }
        if ($$1.shouldSuggestElements()) {
            SharedSuggestionProvider.suggestResource($$0.listElementIds().map(ResourceKey::identifier), $$2);
        }
    }

    public static <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1, ResourceKey<? extends Registry<?>> $$2, ElementSuggestionType $$3) {
        Object object = $$0.getSource();
        if (object instanceof SharedSuggestionProvider) {
            SharedSuggestionProvider $$4 = (SharedSuggestionProvider)object;
            return $$4.suggestRegistryElements($$2, $$3, $$1, $$0);
        }
        return $$1.buildFuture();
    }

    public CompletableFuture<Suggestions> suggestRegistryElements(ResourceKey<? extends Registry<?>> var1, ElementSuggestionType var2, SuggestionsBuilder var3, CommandContext<?> var4);

    public static <T> void filterResources(Iterable<T> $$0, String $$1, Function<T, Identifier> $$2, Consumer<T> $$3) {
        boolean $$4 = $$1.indexOf(58) > -1;
        for (T $$5 : $$0) {
            Identifier $$6 = $$2.apply($$5);
            if ($$4) {
                String $$7 = $$6.toString();
                if (!SharedSuggestionProvider.matchesSubStr($$1, $$7)) continue;
                $$3.accept($$5);
                continue;
            }
            if (!SharedSuggestionProvider.matchesSubStr($$1, $$6.getNamespace()) && !SharedSuggestionProvider.matchesSubStr($$1, $$6.getPath())) continue;
            $$3.accept($$5);
        }
    }

    public static <T> void filterResources(Iterable<T> $$0, String $$1, String $$2, Function<T, Identifier> $$3, Consumer<T> $$4) {
        if ($$1.isEmpty()) {
            $$0.forEach($$4);
        } else {
            String $$5 = Strings.commonPrefix((CharSequence)$$1, (CharSequence)$$2);
            if (!$$5.isEmpty()) {
                String $$6 = $$1.substring($$5.length());
                SharedSuggestionProvider.filterResources($$0, $$6, $$3, $$4);
            }
        }
    }

    public static CompletableFuture<Suggestions> suggestResource(Iterable<Identifier> $$02, SuggestionsBuilder $$1, String $$22) {
        String $$3 = $$1.getRemaining().toLowerCase(Locale.ROOT);
        SharedSuggestionProvider.filterResources($$02, $$3, $$22, $$0 -> $$0, $$2 -> $$1.suggest($$22 + String.valueOf($$2)));
        return $$1.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestResource(Stream<Identifier> $$0, SuggestionsBuilder $$1, String $$2) {
        return SharedSuggestionProvider.suggestResource($$0::iterator, $$1, $$2);
    }

    public static CompletableFuture<Suggestions> suggestResource(Iterable<Identifier> $$02, SuggestionsBuilder $$12) {
        String $$2 = $$12.getRemaining().toLowerCase(Locale.ROOT);
        SharedSuggestionProvider.filterResources($$02, $$2, $$0 -> $$0, $$1 -> $$12.suggest($$1.toString()));
        return $$12.buildFuture();
    }

    public static <T> CompletableFuture<Suggestions> suggestResource(Iterable<T> $$0, SuggestionsBuilder $$1, Function<T, Identifier> $$2, Function<T, Message> $$32) {
        String $$4 = $$1.getRemaining().toLowerCase(Locale.ROOT);
        SharedSuggestionProvider.filterResources($$0, $$4, $$2, $$3 -> $$1.suggest(((Identifier)$$2.apply($$3)).toString(), (Message)$$32.apply($$3)));
        return $$1.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestResource(Stream<Identifier> $$0, SuggestionsBuilder $$1) {
        return SharedSuggestionProvider.suggestResource($$0::iterator, $$1);
    }

    public static <T> CompletableFuture<Suggestions> suggestResource(Stream<T> $$0, SuggestionsBuilder $$1, Function<T, Identifier> $$2, Function<T, Message> $$3) {
        return SharedSuggestionProvider.suggestResource($$0::iterator, $$1, $$2, $$3);
    }

    public static CompletableFuture<Suggestions> suggestCoordinates(String $$0, Collection<TextCoordinates> $$1, SuggestionsBuilder $$2, Predicate<String> $$3) {
        ArrayList $$4;
        block4: {
            String[] $$7;
            block5: {
                block3: {
                    $$4 = Lists.newArrayList();
                    if (!Strings.isNullOrEmpty((String)$$0)) break block3;
                    for (TextCoordinates $$5 : $$1) {
                        String $$6 = $$5.x + " " + $$5.y + " " + $$5.z;
                        if (!$$3.test($$6)) continue;
                        $$4.add($$5.x);
                        $$4.add($$5.x + " " + $$5.y);
                        $$4.add($$6);
                    }
                    break block4;
                }
                $$7 = $$0.split(" ");
                if ($$7.length != 1) break block5;
                for (TextCoordinates $$8 : $$1) {
                    String $$9 = $$7[0] + " " + $$8.y + " " + $$8.z;
                    if (!$$3.test($$9)) continue;
                    $$4.add($$7[0] + " " + $$8.y);
                    $$4.add($$9);
                }
                break block4;
            }
            if ($$7.length != 2) break block4;
            for (TextCoordinates $$10 : $$1) {
                String $$11 = $$7[0] + " " + $$7[1] + " " + $$10.z;
                if (!$$3.test($$11)) continue;
                $$4.add($$11);
            }
        }
        return SharedSuggestionProvider.suggest($$4, $$2);
    }

    public static CompletableFuture<Suggestions> suggest2DCoordinates(String $$0, Collection<TextCoordinates> $$1, SuggestionsBuilder $$2, Predicate<String> $$3) {
        ArrayList $$4;
        block3: {
            block2: {
                $$4 = Lists.newArrayList();
                if (!Strings.isNullOrEmpty((String)$$0)) break block2;
                for (TextCoordinates $$5 : $$1) {
                    String $$6 = $$5.x + " " + $$5.z;
                    if (!$$3.test($$6)) continue;
                    $$4.add($$5.x);
                    $$4.add($$6);
                }
                break block3;
            }
            String[] $$7 = $$0.split(" ");
            if ($$7.length != 1) break block3;
            for (TextCoordinates $$8 : $$1) {
                String $$9 = $$7[0] + " " + $$8.z;
                if (!$$3.test($$9)) continue;
                $$4.add($$9);
            }
        }
        return SharedSuggestionProvider.suggest($$4, $$2);
    }

    public static CompletableFuture<Suggestions> suggest(Iterable<String> $$0, SuggestionsBuilder $$1) {
        String $$2 = $$1.getRemaining().toLowerCase(Locale.ROOT);
        for (String $$3 : $$0) {
            if (!SharedSuggestionProvider.matchesSubStr($$2, $$3.toLowerCase(Locale.ROOT))) continue;
            $$1.suggest($$3);
        }
        return $$1.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggest(Stream<String> $$0, SuggestionsBuilder $$12) {
        String $$2 = $$12.getRemaining().toLowerCase(Locale.ROOT);
        $$0.filter($$1 -> SharedSuggestionProvider.matchesSubStr($$2, $$1.toLowerCase(Locale.ROOT))).forEach(arg_0 -> ((SuggestionsBuilder)$$12).suggest(arg_0));
        return $$12.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggest(String[] $$0, SuggestionsBuilder $$1) {
        String $$2 = $$1.getRemaining().toLowerCase(Locale.ROOT);
        for (String $$3 : $$0) {
            if (!SharedSuggestionProvider.matchesSubStr($$2, $$3.toLowerCase(Locale.ROOT))) continue;
            $$1.suggest($$3);
        }
        return $$1.buildFuture();
    }

    public static <T> CompletableFuture<Suggestions> suggest(Iterable<T> $$0, SuggestionsBuilder $$1, Function<T, String> $$2, Function<T, Message> $$3) {
        String $$4 = $$1.getRemaining().toLowerCase(Locale.ROOT);
        for (T $$5 : $$0) {
            String $$6 = $$2.apply($$5);
            if (!SharedSuggestionProvider.matchesSubStr($$4, $$6.toLowerCase(Locale.ROOT))) continue;
            $$1.suggest($$6, $$3.apply($$5));
        }
        return $$1.buildFuture();
    }

    public static boolean matchesSubStr(String $$0, String $$1) {
        int $$2 = 0;
        while (!$$1.startsWith($$0, $$2)) {
            int $$3 = MATCH_SPLITTER.indexIn((CharSequence)$$1, $$2);
            if ($$3 < 0) {
                return false;
            }
            $$2 = $$3 + 1;
        }
        return true;
    }

    public static class TextCoordinates {
        public static final TextCoordinates DEFAULT_LOCAL = new TextCoordinates("^", "^", "^");
        public static final TextCoordinates DEFAULT_GLOBAL = new TextCoordinates("~", "~", "~");
        public final String x;
        public final String y;
        public final String z;

        public TextCoordinates(String $$0, String $$1, String $$2) {
            this.x = $$0;
            this.y = $$1;
            this.z = $$2;
        }
    }

    public static enum ElementSuggestionType {
        TAGS,
        ELEMENTS,
        ALL;


        public boolean shouldSuggestTags() {
            return this == TAGS || this == ALL;
        }

        public boolean shouldSuggestElements() {
            return this == ELEMENTS || this == ALL;
        }
    }
}

