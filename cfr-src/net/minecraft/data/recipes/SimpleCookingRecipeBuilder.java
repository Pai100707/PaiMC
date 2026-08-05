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
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public class SimpleCookingRecipeBuilder
implements RecipeBuilder {
    private final RecipeCategory category;
    private final CookingBookCategory bookCategory;
    private final Item result;
    private final Ingredient ingredient;
    private final float experience;
    private final int cookingTime;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
    private @Nullable String group;
    private final AbstractCookingRecipe.Factory<?> factory;

    private SimpleCookingRecipeBuilder(RecipeCategory $$0, CookingBookCategory $$1, ItemLike $$2, Ingredient $$3, float $$4, int $$5, AbstractCookingRecipe.Factory<?> $$6) {
        this.category = $$0;
        this.bookCategory = $$1;
        this.result = $$2.asItem();
        this.ingredient = $$3;
        this.experience = $$4;
        this.cookingTime = $$5;
        this.factory = $$6;
    }

    public static <T extends AbstractCookingRecipe> SimpleCookingRecipeBuilder generic(Ingredient $$0, RecipeCategory $$1, ItemLike $$2, float $$3, int $$4, RecipeSerializer<T> $$5, AbstractCookingRecipe.Factory<T> $$6) {
        return new SimpleCookingRecipeBuilder($$1, SimpleCookingRecipeBuilder.determineRecipeCategory($$5, $$2), $$2, $$0, $$3, $$4, $$6);
    }

    public static SimpleCookingRecipeBuilder campfireCooking(Ingredient $$0, RecipeCategory $$1, ItemLike $$2, float $$3, int $$4) {
        return new SimpleCookingRecipeBuilder($$1, CookingBookCategory.FOOD, $$2, $$0, $$3, $$4, CampfireCookingRecipe::new);
    }

    public static SimpleCookingRecipeBuilder blasting(Ingredient $$0, RecipeCategory $$1, ItemLike $$2, float $$3, int $$4) {
        return new SimpleCookingRecipeBuilder($$1, SimpleCookingRecipeBuilder.determineBlastingRecipeCategory($$2), $$2, $$0, $$3, $$4, BlastingRecipe::new);
    }

    public static SimpleCookingRecipeBuilder smelting(Ingredient $$0, RecipeCategory $$1, ItemLike $$2, float $$3, int $$4) {
        return new SimpleCookingRecipeBuilder($$1, SimpleCookingRecipeBuilder.determineSmeltingRecipeCategory($$2), $$2, $$0, $$3, $$4, SmeltingRecipe::new);
    }

    public static SimpleCookingRecipeBuilder smoking(Ingredient $$0, RecipeCategory $$1, ItemLike $$2, float $$3, int $$4) {
        return new SimpleCookingRecipeBuilder($$1, CookingBookCategory.FOOD, $$2, $$0, $$3, $$4, SmokingRecipe::new);
    }

    @Override
    public SimpleCookingRecipeBuilder unlockedBy(String $$0, Criterion<?> $$1) {
        this.criteria.put($$0, $$1);
        return this;
    }

    @Override
    public SimpleCookingRecipeBuilder group(@Nullable String $$0) {
        this.group = $$0;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput $$0, ResourceKey<Recipe<?>> $$1) {
        this.ensureValid($$1);
        Advancement.Builder $$2 = $$0.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked($$1)).rewards(AdvancementRewards.Builder.recipe($$1)).requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach($$2::addCriterion);
        Object $$3 = this.factory.create(Objects.requireNonNullElse(this.group, ""), this.bookCategory, this.ingredient, new ItemStack(this.result), this.experience, this.cookingTime);
        $$0.accept($$1, (Recipe<?>)$$3, $$2.build($$1.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private static CookingBookCategory determineSmeltingRecipeCategory(ItemLike $$0) {
        if ($$0.asItem().components().has(DataComponents.FOOD)) {
            return CookingBookCategory.FOOD;
        }
        if ($$0.asItem() instanceof BlockItem) {
            return CookingBookCategory.BLOCKS;
        }
        return CookingBookCategory.MISC;
    }

    private static CookingBookCategory determineBlastingRecipeCategory(ItemLike $$0) {
        if ($$0.asItem() instanceof BlockItem) {
            return CookingBookCategory.BLOCKS;
        }
        return CookingBookCategory.MISC;
    }

    private static CookingBookCategory determineRecipeCategory(RecipeSerializer<? extends AbstractCookingRecipe> $$0, ItemLike $$1) {
        if ($$0 == RecipeSerializer.SMELTING_RECIPE) {
            return SimpleCookingRecipeBuilder.determineSmeltingRecipeCategory($$1);
        }
        if ($$0 == RecipeSerializer.BLASTING_RECIPE) {
            return SimpleCookingRecipeBuilder.determineBlastingRecipeCategory($$1);
        }
        if ($$0 == RecipeSerializer.SMOKING_RECIPE || $$0 == RecipeSerializer.CAMPFIRE_COOKING_RECIPE) {
            return CookingBookCategory.FOOD;
        }
        throw new IllegalStateException("Unknown cooking recipe type");
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

