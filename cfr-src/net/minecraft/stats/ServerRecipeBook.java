/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.stats;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import org.slf4j.Logger;

public class ServerRecipeBook
extends RecipeBook {
    public static final String RECIPE_BOOK_TAG = "recipeBook";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DisplayResolver displayResolver;
    @VisibleForTesting
    protected final Set<ResourceKey<Recipe<?>>> known = Sets.newIdentityHashSet();
    @VisibleForTesting
    protected final Set<ResourceKey<Recipe<?>>> highlight = Sets.newIdentityHashSet();

    public ServerRecipeBook(DisplayResolver $$0) {
        this.displayResolver = $$0;
    }

    public void add(ResourceKey<Recipe<?>> $$0) {
        this.known.add($$0);
    }

    public boolean contains(ResourceKey<Recipe<?>> $$0) {
        return this.known.contains($$0);
    }

    public void remove(ResourceKey<Recipe<?>> $$0) {
        this.known.remove($$0);
        this.highlight.remove($$0);
    }

    public void removeHighlight(ResourceKey<Recipe<?>> $$0) {
        this.highlight.remove($$0);
    }

    private void addHighlight(ResourceKey<Recipe<?>> $$0) {
        this.highlight.add($$0);
    }

    public int addRecipes(Collection<RecipeHolder<?>> $$0, ServerPlayer $$1) {
        ArrayList<ClientboundRecipeBookAddPacket.Entry> $$22 = new ArrayList<ClientboundRecipeBookAddPacket.Entry>();
        for (RecipeHolder<?> $$3 : $$0) {
            ResourceKey<Recipe<?>> $$4 = $$3.id();
            if (this.known.contains($$4) || $$3.value().isSpecial()) continue;
            this.add($$4);
            this.addHighlight($$4);
            this.displayResolver.displaysForRecipe($$4, $$2 -> $$22.add(new ClientboundRecipeBookAddPacket.Entry((RecipeDisplayEntry)$$2, $$3.value().showNotification(), true)));
            CriteriaTriggers.RECIPE_UNLOCKED.trigger($$1, $$3);
        }
        if (!$$22.isEmpty()) {
            $$1.connection.send(new ClientboundRecipeBookAddPacket($$22, false));
        }
        return $$22.size();
    }

    public int removeRecipes(Collection<RecipeHolder<?>> $$0, ServerPlayer $$12) {
        ArrayList $$2 = Lists.newArrayList();
        for (RecipeHolder<?> $$3 : $$0) {
            ResourceKey<Recipe<?>> $$4 = $$3.id();
            if (!this.known.contains($$4)) continue;
            this.remove($$4);
            this.displayResolver.displaysForRecipe($$4, $$1 -> $$2.add($$1.id()));
        }
        if (!$$2.isEmpty()) {
            $$12.connection.send(new ClientboundRecipeBookRemovePacket($$2));
        }
        return $$2.size();
    }

    private void loadRecipes(List<ResourceKey<Recipe<?>>> $$0, Consumer<ResourceKey<Recipe<?>>> $$1, Predicate<ResourceKey<Recipe<?>>> $$2) {
        for (ResourceKey<Recipe<?>> $$3 : $$0) {
            if (!$$2.test($$3)) {
                LOGGER.error("Tried to load unrecognized recipe: {} removed now.", $$3);
                continue;
            }
            $$1.accept($$3);
        }
    }

    public void sendInitialRecipeBook(ServerPlayer $$0) {
        $$0.connection.send(new ClientboundRecipeBookSettingsPacket(this.getBookSettings().copy()));
        ArrayList<ClientboundRecipeBookAddPacket.Entry> $$1 = new ArrayList<ClientboundRecipeBookAddPacket.Entry>(this.known.size());
        for (ResourceKey<Recipe<?>> $$22 : this.known) {
            this.displayResolver.displaysForRecipe($$22, $$2 -> $$1.add(new ClientboundRecipeBookAddPacket.Entry((RecipeDisplayEntry)$$2, false, this.highlight.contains($$22))));
        }
        $$0.connection.send(new ClientboundRecipeBookAddPacket($$1, true));
    }

    public void copyOverData(ServerRecipeBook $$0) {
        this.apply($$0.pack());
    }

    public Packed pack() {
        return new Packed(this.bookSettings.copy(), List.copyOf(this.known), List.copyOf(this.highlight));
    }

    private void apply(Packed $$0) {
        this.known.clear();
        this.highlight.clear();
        this.bookSettings.replaceFrom($$0.settings);
        this.known.addAll($$0.known);
        this.highlight.addAll($$0.highlight);
    }

    public void loadUntrusted(Packed $$0, Predicate<ResourceKey<Recipe<?>>> $$1) {
        this.bookSettings.replaceFrom($$0.settings);
        this.loadRecipes($$0.known, this.known::add, $$1);
        this.loadRecipes($$0.highlight, this.highlight::add, $$1);
    }

    @FunctionalInterface
    public static interface DisplayResolver {
        public void displaysForRecipe(ResourceKey<Recipe<?>> var1, Consumer<RecipeDisplayEntry> var2);
    }

    public static final class Packed
    extends Record {
        final RecipeBookSettings settings;
        final List<ResourceKey<Recipe<?>>> known;
        final List<ResourceKey<Recipe<?>>> highlight;
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)RecipeBookSettings.MAP_CODEC.forGetter(Packed::settings), (App)Recipe.KEY_CODEC.listOf().fieldOf("recipes").forGetter(Packed::known), (App)Recipe.KEY_CODEC.listOf().fieldOf("toBeDisplayed").forGetter(Packed::highlight)).apply((Applicative)$$0, Packed::new));

        public Packed(RecipeBookSettings $$0, List<ResourceKey<Recipe<?>>> $$1, List<ResourceKey<Recipe<?>>> $$2) {
            this.settings = $$0;
            this.known = $$1;
            this.highlight = $$2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "settings;known;highlight", "settings", "known", "highlight"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "settings;known;highlight", "settings", "known", "highlight"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "settings;known;highlight", "settings", "known", "highlight"}, this, $$0);
        }

        public RecipeBookSettings settings() {
            return this.settings;
        }

        public List<ResourceKey<Recipe<?>>> known() {
            return this.known;
        }

        public List<ResourceKey<Recipe<?>>> highlight() {
            return this.highlight;
        }
    }
}

