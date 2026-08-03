package net.minecraft.world.item.crafting;

public record SingleRecipeInput(net.minecraft.world.item.ItemStack item) implements RecipeInput {
   @Override
   public net.minecraft.world.item.ItemStack getItem(int $$0) {
      if ($$0 != 0) {
         throw new IllegalArgumentException("No item for index " + $$0);
      } else {
         return this.item;
      }
   }

   @Override
   public int size() {
      return 1;
   }
}
