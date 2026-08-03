package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class ShieldDecorationRecipe extends CustomRecipe {
   public ShieldDecorationRecipe(CraftingBookCategory $$0) {
      super($$0);
   }

   public boolean matches(CraftingInput $$0, Level $$1) {
      if ($$0.ingredientCount() != 2) {
         return false;
      } else {
         boolean $$2 = false;
         boolean $$3 = false;

         for (int $$4 = 0; $$4 < $$0.size(); $$4++) {
            net.minecraft.world.item.ItemStack $$5 = $$0.getItem($$4);
            if (!$$5.isEmpty()) {
               if ($$5.getItem() instanceof net.minecraft.world.item.BannerItem) {
                  if ($$3) {
                     return false;
                  }

                  $$3 = true;
               } else {
                  if (!$$5.is(net.minecraft.world.item.Items.SHIELD)) {
                     return false;
                  }

                  if ($$2) {
                     return false;
                  }

                  BannerPatternLayers $$6 = (BannerPatternLayers)$$5.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
                  if (!$$6.layers().isEmpty()) {
                     return false;
                  }

                  $$2 = true;
               }
            }
         }

         return $$2 && $$3;
      }
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      net.minecraft.world.item.ItemStack $$2 = net.minecraft.world.item.ItemStack.EMPTY;
      net.minecraft.world.item.ItemStack $$3 = net.minecraft.world.item.ItemStack.EMPTY;

      for (int $$4 = 0; $$4 < $$0.size(); $$4++) {
         net.minecraft.world.item.ItemStack $$5 = $$0.getItem($$4);
         if (!$$5.isEmpty()) {
            if ($$5.getItem() instanceof net.minecraft.world.item.BannerItem) {
               $$2 = $$5;
            } else if ($$5.is(net.minecraft.world.item.Items.SHIELD)) {
               $$3 = $$5.copy();
            }
         }
      }

      if ($$3.isEmpty()) {
         return $$3;
      } else {
         $$3.set(DataComponents.BANNER_PATTERNS, (BannerPatternLayers)$$2.get(DataComponents.BANNER_PATTERNS));
         $$3.set(DataComponents.BASE_COLOR, ((net.minecraft.world.item.BannerItem)$$2.getItem()).getColor());
         return $$3;
      }
   }

   @Override
   public RecipeSerializer<ShieldDecorationRecipe> getSerializer() {
      return RecipeSerializer.SHIELD_DECORATION;
   }
}
