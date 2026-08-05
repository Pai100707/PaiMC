/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.recipes;

import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;

public class SpecialRecipeBuilder {
    private final Function<CraftingBookCategory, Recipe<?>> factory;

    public SpecialRecipeBuilder(Function<CraftingBookCategory, Recipe<?>> $$0) {
        this.factory = $$0;
    }

    public static SpecialRecipeBuilder special(Function<CraftingBookCategory, Recipe<?>> $$0) {
        return new SpecialRecipeBuilder($$0);
    }

    public void save(RecipeOutput $$0, String $$1) {
        this.save($$0, ResourceKey.create(Registries.RECIPE, Identifier.parse($$1)));
    }

    public void save(RecipeOutput $$0, ResourceKey<Recipe<?>> $$1) {
        $$0.accept($$1, this.factory.apply(CraftingBookCategory.MISC), null);
    }
}

