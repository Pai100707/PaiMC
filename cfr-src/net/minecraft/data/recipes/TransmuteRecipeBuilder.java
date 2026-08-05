/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.minecraft.world.item.crafting.TransmuteResult;
import org.jspecify.annotations.Nullable;

public class TransmuteRecipeBuilder
implements RecipeBuilder {
    private final RecipeCategory category;
    private final Holder<Item> result;
    private final Ingredient input;
    private final Ingredient material;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
    private @Nullable String group;

    private TransmuteRecipeBuilder(RecipeCategory $$0, Holder<Item> $$1, Ingredient $$2, Ingredient $$3) {
        this.category = $$0;
        this.result = $$1;
        this.input = $$2;
        this.material = $$3;
    }

    public static TransmuteRecipeBuilder transmute(RecipeCategory $$0, Ingredient $$1, Ingredient $$2, Item $$3) {
        return new TransmuteRecipeBuilder($$0, $$3.builtInRegistryHolder(), $$1, $$2);
    }

    @Override
    public TransmuteRecipeBuilder unlockedBy(String $$0, Criterion<?> $$1) {
        this.criteria.put($$0, $$1);
        return this;
    }

    @Override
    public TransmuteRecipeBuilder group(@Nullable String $$0) {
        this.group = $$0;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result.value();
    }

    @Override
    public void save(RecipeOutput $$0, ResourceKey<Recipe<?>> $$1) {
        this.ensureValid($$1);
        Advancement.Builder $$2 = $$0.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked($$1)).rewards(AdvancementRewards.Builder.recipe($$1)).requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach($$2::addCriterion);
        TransmuteRecipe $$3 = new TransmuteRecipe(Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), this.input, this.material, new TransmuteResult(this.result.value()));
        $$0.accept($$1, $$3, $$2.build($$1.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceKey<Recipe<?>> $$0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + String.valueOf($$0.identifier()));
        }
    }

    @Override
    public /* synthetic */ RecipeBuilder group(@Nullable String string) {
        return this.group(string);
    }

    public /* synthetic */ RecipeBuilder unlockedBy(String string, Criterion criterion) {
        return this.unlockedBy(string, criterion);
    }
}

