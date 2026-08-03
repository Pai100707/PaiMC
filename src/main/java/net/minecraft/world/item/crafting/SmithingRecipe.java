package net.minecraft.world.item.crafting;

import java.util.Optional;
import net.minecraft.world.level.Level;

public interface SmithingRecipe extends Recipe<SmithingRecipeInput> {
   @Override
   default RecipeType<SmithingRecipe> getType() {
      return RecipeType.SMITHING;
   }

   @Override
   RecipeSerializer<? extends SmithingRecipe> getSerializer();

   default boolean matches(SmithingRecipeInput $$0, Level $$1) {
      return Ingredient.testOptionalIngredient(this.templateIngredient(), $$0.template())
         && this.baseIngredient().test($$0.base())
         && Ingredient.testOptionalIngredient(this.additionIngredient(), $$0.addition());
   }

   Optional<Ingredient> templateIngredient();

   Ingredient baseIngredient();

   Optional<Ingredient> additionIngredient();

   @Override
   default RecipeBookCategory recipeBookCategory() {
      return RecipeBookCategories.SMITHING;
   }
}
