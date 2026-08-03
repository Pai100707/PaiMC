package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;

public interface CraftingRecipe extends Recipe<CraftingInput> {
   @Override
   default RecipeType<CraftingRecipe> getType() {
      return RecipeType.CRAFTING;
   }

   @Override
   RecipeSerializer<? extends CraftingRecipe> getSerializer();

   CraftingBookCategory category();

   default NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(CraftingInput $$0) {
      return defaultCraftingReminder($$0);
   }

   static NonNullList<net.minecraft.world.item.ItemStack> defaultCraftingReminder(CraftingInput $$0) {
      NonNullList<net.minecraft.world.item.ItemStack> $$1 = NonNullList.withSize($$0.size(), net.minecraft.world.item.ItemStack.EMPTY);

      for (int $$2 = 0; $$2 < $$1.size(); $$2++) {
         net.minecraft.world.item.Item $$3 = $$0.getItem($$2).getItem();
         $$1.set($$2, $$3.getCraftingRemainder());
      }

      return $$1;
   }

   @Override
   default RecipeBookCategory recipeBookCategory() {
      return switch (this.category()) {
         case BUILDING -> RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
         case EQUIPMENT -> RecipeBookCategories.CRAFTING_EQUIPMENT;
         case REDSTONE -> RecipeBookCategories.CRAFTING_REDSTONE;
         case MISC -> RecipeBookCategories.CRAFTING_MISC;
      };
   }
}
