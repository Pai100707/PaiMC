package net.minecraft.world.item.crafting;

public class SmokingRecipe extends AbstractCookingRecipe {
   public SmokingRecipe(String $$0, CookingBookCategory $$1, Ingredient $$2, net.minecraft.world.item.ItemStack $$3, float $$4, int $$5) {
      super($$0, $$1, $$2, $$3, $$4, $$5);
   }

   @Override
   protected net.minecraft.world.item.Item furnaceIcon() {
      return net.minecraft.world.item.Items.SMOKER;
   }

   @Override
   public RecipeType<SmokingRecipe> getType() {
      return RecipeType.SMOKING;
   }

   @Override
   public RecipeSerializer<SmokingRecipe> getSerializer() {
      return RecipeSerializer.SMOKING_RECIPE;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return RecipeBookCategories.SMOKER_FOOD;
   }
}
