package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public interface PlaceRecipeHelper {
   static <T> void placeRecipe(int $$0, int $$1, Recipe<?> $$2, Iterable<T> $$3, net.minecraft.recipebook.PlaceRecipeHelper.Output<T> $$4) {
      if ($$2 instanceof ShapedRecipe $$5) {
         placeRecipe($$0, $$1, $$5.getWidth(), $$5.getHeight(), $$3, $$4);
      } else {
         placeRecipe($$0, $$1, $$0, $$1, $$3, $$4);
      }
   }

   static <T> void placeRecipe(int $$0, int $$1, int $$2, int $$3, Iterable<T> $$4, net.minecraft.recipebook.PlaceRecipeHelper.Output<T> $$5) {
      Iterator<T> $$6 = $$4.iterator();
      int $$7 = 0;

      for (int $$8 = 0; $$8 < $$1; $$8++) {
         boolean $$9 = $$3 < $$1 / 2.0F;
         int $$10 = Mth.floor($$1 / 2.0F - $$3 / 2.0F);
         if ($$9 && $$10 > $$8) {
            $$7 += $$0;
            $$8++;
         }

         for (int $$11 = 0; $$11 < $$0; $$11++) {
            if (!$$6.hasNext()) {
               return;
            }

            $$9 = $$2 < $$0 / 2.0F;
            $$10 = Mth.floor($$0 / 2.0F - $$2 / 2.0F);
            int $$12 = $$2;
            boolean $$13 = $$11 < $$2;
            if ($$9) {
               $$12 = $$10 + $$2;
               $$13 = $$10 <= $$11 && $$11 < $$10 + $$2;
            }

            if ($$13) {
               $$5.addItemToSlot($$6.next(), $$7, $$11, $$8);
            } else if ($$12 == $$11) {
               $$7 += $$0 - $$11;
               break;
            }

            $$7++;
         }
      }
   }

   @FunctionalInterface
   public interface Output<T> {
      void addItemToSlot(T var1, int var2, int var3, int var4);
   }
}
