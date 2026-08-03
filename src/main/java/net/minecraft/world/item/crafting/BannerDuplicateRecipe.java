package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class BannerDuplicateRecipe extends CustomRecipe {
   public BannerDuplicateRecipe(CraftingBookCategory $$0) {
      super($$0);
   }

   public boolean matches(CraftingInput $$0, Level $$1) {
      if ($$0.ingredientCount() != 2) {
         return false;
      } else {
         net.minecraft.world.item.DyeColor $$2 = null;
         boolean $$3 = false;
         boolean $$4 = false;

         for (int $$5 = 0; $$5 < $$0.size(); $$5++) {
            net.minecraft.world.item.ItemStack $$6 = $$0.getItem($$5);
            if (!$$6.isEmpty()) {
               if (!($$6.getItem() instanceof net.minecraft.world.item.BannerItem $$8)) {
                  return false;
               }

               if ($$2 == null) {
                  $$2 = $$8.getColor();
               } else if ($$2 != $$8.getColor()) {
                  return false;
               }

               int $$10 = ((BannerPatternLayers)$$6.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)).layers().size();
               if ($$10 > 6) {
                  return false;
               }

               if ($$10 > 0) {
                  if ($$4) {
                     return false;
                  }

                  $$4 = true;
               } else {
                  if ($$3) {
                     return false;
                  }

                  $$3 = true;
               }
            }
         }

         return $$4 && $$3;
      }
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      for (int $$2 = 0; $$2 < $$0.size(); $$2++) {
         net.minecraft.world.item.ItemStack $$3 = $$0.getItem($$2);
         if (!$$3.isEmpty()) {
            int $$4 = ((BannerPatternLayers)$$3.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)).layers().size();
            if ($$4 > 0 && $$4 <= 6) {
               return $$3.copyWithCount(1);
            }
         }
      }

      return net.minecraft.world.item.ItemStack.EMPTY;
   }

   @Override
   public NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(CraftingInput $$0) {
      NonNullList<net.minecraft.world.item.ItemStack> $$1 = NonNullList.withSize($$0.size(), net.minecraft.world.item.ItemStack.EMPTY);

      for (int $$2 = 0; $$2 < $$1.size(); $$2++) {
         net.minecraft.world.item.ItemStack $$3 = $$0.getItem($$2);
         if (!$$3.isEmpty()) {
            net.minecraft.world.item.ItemStack $$4 = $$3.getItem().getCraftingRemainder();
            if (!$$4.isEmpty()) {
               $$1.set($$2, $$4);
            } else if (!((BannerPatternLayers)$$3.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)).layers().isEmpty()) {
               $$1.set($$2, $$3.copyWithCount(1));
            }
         }
      }

      return $$1;
   }

   @Override
   public RecipeSerializer<BannerDuplicateRecipe> getSerializer() {
      return RecipeSerializer.BANNER_DUPLICATE;
   }
}
