package net.minecraft.world.item.crafting;

public class SmeltingRecipe extends AbstractCookingRecipe {
   public SmeltingRecipe(String $$0, CookingBookCategory $$1, Ingredient $$2, net.minecraft.world.item.ItemStack $$3, float $$4, int $$5) {
      super($$0, $$1, $$2, $$3, $$4, $$5);
   }

   @Override
   protected net.minecraft.world.item.Item furnaceIcon() {
      return net.minecraft.world.item.Items.FURNACE;
   }

   @Override
   public RecipeSerializer<SmeltingRecipe> getSerializer() {
      return RecipeSerializer.SMELTING_RECIPE;
   }

   @Override
   public RecipeType<SmeltingRecipe> getType() {
      return RecipeType.SMELTING;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return switch (this.category()) {
         case BLOCKS -> RecipeBookCategories.FURNACE_BLOCKS;
         case FOOD -> RecipeBookCategories.FURNACE_FOOD;
         case MISC -> RecipeBookCategories.FURNACE_MISC;
      };
   }
}
