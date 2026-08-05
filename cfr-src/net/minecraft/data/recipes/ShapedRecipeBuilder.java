/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public class ShapedRecipeBuilder
implements RecipeBuilder {
    private final HolderGetter<Item> items;
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
    private @Nullable String group;
    private boolean showNotification = true;

    private ShapedRecipeBuilder(HolderGetter<Item> $$0, RecipeCategory $$1, ItemLike $$2, int $$3) {
        this.items = $$0;
        this.category = $$1;
        this.result = $$2.asItem();
        this.count = $$3;
    }

    public static ShapedRecipeBuilder shaped(HolderGetter<Item> $$0, RecipeCategory $$1, ItemLike $$2) {
        return ShapedRecipeBuilder.shaped($$0, $$1, $$2, 1);
    }

    public static ShapedRecipeBuilder shaped(HolderGetter<Item> $$0, RecipeCategory $$1, ItemLike $$2, int $$3) {
        return new ShapedRecipeBuilder($$0, $$1, $$2, $$3);
    }

    public ShapedRecipeBuilder define(Character $$0, TagKey<Item> $$1) {
        return this.define($$0, Ingredient.of(this.items.getOrThrow($$1)));
    }

    public ShapedRecipeBuilder define(Character $$0, ItemLike $$1) {
        return this.define($$0, Ingredient.of($$1));
    }

    public ShapedRecipeBuilder define(Character $$0, Ingredient $$1) {
        if (this.key.containsKey($$0)) {
            throw new IllegalArgumentException("Symbol '" + $$0 + "' is already defined!");
        }
        if ($$0.charValue() == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        }
        this.key.put($$0, $$1);
        return this;
    }

    public ShapedRecipeBuilder pattern(String $$0) {
        if (!this.rows.isEmpty() && $$0.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        }
        this.rows.add($$0);
        return this;
    }

    @Override
    public ShapedRecipeBuilder unlockedBy(String $$0, Criterion<?> $$1) {
        this.criteria.put($$0, $$1);
        return this;
    }

    @Override
    public ShapedRecipeBuilder group(@Nullable String $$0) {
        this.group = $$0;
        return this;
    }

    public ShapedRecipeBuilder showNotification(boolean $$0) {
        this.showNotification = $$0;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput $$0, ResourceKey<Recipe<?>> $$1) {
        ShapedRecipePattern $$2 = this.ensureValid($$1);
        Advancement.Builder $$3 = $$0.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked($$1)).rewards(AdvancementRewards.Builder.recipe($$1)).requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach($$3::addCriterion);
        ShapedRecipe $$4 = new ShapedRecipe(Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), $$2, new ItemStack(this.result, this.count), this.showNotification);
        $$0.accept($$1, $$4, $$3.build($$1.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private ShapedRecipePattern ensureValid(ResourceKey<Recipe<?>> $$0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + String.valueOf($$0.identifier()));
        }
        return ShapedRecipePattern.of(this.key, this.rows);
    }

    @Override
    public /* synthetic */ RecipeBuilder group(@Nullable String string) {
        return this.group(string);
    }

    public /* synthetic */ RecipeBuilder unlockedBy(String string, Criterion criterion) {
        return this.unlockedBy(string, criterion);
    }
}

