package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;

public class MapCloningRecipe extends CustomRecipe {
   public MapCloningRecipe(CraftingBookCategory $$0) {
      super($$0);
   }

   public boolean matches(CraftingInput $$0, Level $$1) {
      if ($$0.ingredientCount() < 2) {
         return false;
      } else {
         boolean $$2 = false;
         boolean $$3 = false;

         for (int $$4 = 0; $$4 < $$0.size(); $$4++) {
            net.minecraft.world.item.ItemStack $$5 = $$0.getItem($$4);
            if (!$$5.isEmpty()) {
               if ($$5.has(DataComponents.MAP_ID)) {
                  if ($$3) {
                     return false;
                  }

                  $$3 = true;
               } else {
                  if (!$$5.is(net.minecraft.world.item.Items.MAP)) {
                     return false;
                  }

                  $$2 = true;
               }
            }
         }

         return $$3 && $$2;
      }
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      int $$2 = 0;
      net.minecraft.world.item.ItemStack $$3 = net.minecraft.world.item.ItemStack.EMPTY;

      for (int $$4 = 0; $$4 < $$0.size(); $$4++) {
         net.minecraft.world.item.ItemStack $$5 = $$0.getItem($$4);
         if (!$$5.isEmpty()) {
            if ($$5.has(DataComponents.MAP_ID)) {
               if (!$$3.isEmpty()) {
                  return net.minecraft.world.item.ItemStack.EMPTY;
               }

               $$3 = $$5;
            } else {
               if (!$$5.is(net.minecraft.world.item.Items.MAP)) {
                  return net.minecraft.world.item.ItemStack.EMPTY;
               }

               $$2++;
            }
         }
      }

      return !$$3.isEmpty() && $$2 >= 1 ? $$3.copyWithCount($$2 + 1) : net.minecraft.world.item.ItemStack.EMPTY;
   }

   @Override
   public RecipeSerializer<MapCloningRecipe> getSerializer() {
      return RecipeSerializer.MAP_CLONING;
   }
}
