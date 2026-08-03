package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;

public class FireworkStarFadeRecipe extends CustomRecipe {
   private static final Ingredient STAR_INGREDIENT = Ingredient.of(net.minecraft.world.item.Items.FIREWORK_STAR);

   public FireworkStarFadeRecipe(CraftingBookCategory $$0) {
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
               if ($$5.getItem() instanceof net.minecraft.world.item.DyeItem) {
                  $$2 = true;
               } else {
                  if (!STAR_INGREDIENT.test($$5)) {
                     return false;
                  }

                  if ($$3) {
                     return false;
                  }

                  $$3 = true;
               }
            }
         }

         return $$3 && $$2;
      }
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      IntList $$2 = new IntArrayList();
      net.minecraft.world.item.ItemStack $$3 = null;

      for (int $$4 = 0; $$4 < $$0.size(); $$4++) {
         net.minecraft.world.item.ItemStack $$5 = $$0.getItem($$4);
         if ($$5.getItem() instanceof net.minecraft.world.item.DyeItem $$7) {
            $$2.add($$7.getDyeColor().getFireworkColor());
         } else if (STAR_INGREDIENT.test($$5)) {
            $$3 = $$5.copyWithCount(1);
         }
      }

      if ($$3 != null && !$$2.isEmpty()) {
         $$3.update(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion.DEFAULT, $$2, FireworkExplosion::withFadeColors);
         return $$3;
      } else {
         return net.minecraft.world.item.ItemStack.EMPTY;
      }
   }

   @Override
   public RecipeSerializer<FireworkStarFadeRecipe> getSerializer() {
      return RecipeSerializer.FIREWORK_STAR_FADE;
   }
}
