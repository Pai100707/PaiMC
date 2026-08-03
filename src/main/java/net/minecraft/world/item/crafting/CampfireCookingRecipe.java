package net.minecraft.world.item.crafting;

public class CampfireCookingRecipe extends AbstractCookingRecipe {
   public CampfireCookingRecipe(String $$0, CookingBookCategory $$1, Ingredient $$2, net.minecraft.world.item.ItemStack $$3, float $$4, int $$5) {
      super($$0, $$1, $$2, $$3, $$4, $$5);
   }

   @Override
   protected net.minecraft.world.item.Item furnaceIcon() {
      return net.minecraft.world.item.Items.CAMPFIRE;
   }

   @Override
   public RecipeSerializer<CampfireCookingRecipe> getSerializer() {
      return RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
   }

   @Override
   public RecipeType<CampfireCookingRecipe> getType() {
      return RecipeType.CAMPFIRE_COOKING;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return RecipeBookCategories.CAMPFIRE;
   }
}
