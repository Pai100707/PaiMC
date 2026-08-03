package net.minecraft.world.item.crafting;

public interface RecipeInput {
   net.minecraft.world.item.ItemStack getItem(int var1);

   int size();

   default boolean isEmpty() {
      for (int $$0 = 0; $$0 < this.size(); $$0++) {
         if (!this.getItem($$0).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}
