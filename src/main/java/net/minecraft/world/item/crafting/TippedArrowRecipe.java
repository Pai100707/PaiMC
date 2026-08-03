package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

public class TippedArrowRecipe extends CustomRecipe {
   public TippedArrowRecipe(CraftingBookCategory $$0) {
      super($$0);
   }

   public boolean matches(CraftingInput $$0, Level $$1) {
      if ($$0.width() == 3 && $$0.height() == 3 && $$0.ingredientCount() == 9) {
         for (int $$2 = 0; $$2 < $$0.height(); $$2++) {
            for (int $$3 = 0; $$3 < $$0.width(); $$3++) {
               net.minecraft.world.item.ItemStack $$4 = $$0.getItem($$3, $$2);
               if ($$4.isEmpty()) {
                  return false;
               }

               if ($$3 == 1 && $$2 == 1) {
                  if (!$$4.is(net.minecraft.world.item.Items.LINGERING_POTION)) {
                     return false;
                  }
               } else if (!$$4.is(net.minecraft.world.item.Items.ARROW)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      net.minecraft.world.item.ItemStack $$2 = $$0.getItem(1, 1);
      if (!$$2.is(net.minecraft.world.item.Items.LINGERING_POTION)) {
         return net.minecraft.world.item.ItemStack.EMPTY;
      } else {
         net.minecraft.world.item.ItemStack $$3 = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TIPPED_ARROW, 8);
         $$3.set(DataComponents.POTION_CONTENTS, (PotionContents)$$2.get(DataComponents.POTION_CONTENTS));
         return $$3;
      }
   }

   @Override
   public RecipeSerializer<TippedArrowRecipe> getSerializer() {
      return RecipeSerializer.TIPPED_ARROW;
   }
}
