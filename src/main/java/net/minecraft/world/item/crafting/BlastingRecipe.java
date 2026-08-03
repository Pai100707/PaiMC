package net.minecraft.world.item.crafting;

public class BlastingRecipe extends AbstractCookingRecipe {
   public BlastingRecipe(String $$0, CookingBookCategory $$1, Ingredient $$2, net.minecraft.world.item.ItemStack $$3, float $$4, int $$5) {
      super($$0, $$1, $$2, $$3, $$4, $$5);
   }

   @Override
   protected net.minecraft.world.item.Item furnaceIcon() {
      return net.minecraft.world.item.Items.BLAST_FURNACE;
   }

   @Override
   public RecipeSerializer<BlastingRecipe> getSerializer() {
      return RecipeSerializer.BLASTING_RECIPE;
   }

   @Override
   public RecipeType<BlastingRecipe> getType() {
      return RecipeType.BLASTING;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return switch (this.category()) {
         case BLOCKS -> RecipeBookCategories.BLAST_FURNACE_BLOCKS;
         case FOOD, MISC -> RecipeBookCategories.BLAST_FURNACE_MISC;
      };
   }
}
