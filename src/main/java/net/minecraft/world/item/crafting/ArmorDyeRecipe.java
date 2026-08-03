package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;

public class ArmorDyeRecipe extends CustomRecipe {
   public ArmorDyeRecipe(CraftingBookCategory $$0) {
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
               if ($$5.is(ItemTags.DYEABLE)) {
                  if ($$2) {
                     return false;
                  }

                  $$2 = true;
               } else {
                  if (!($$5.getItem() instanceof net.minecraft.world.item.DyeItem)) {
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
      List<net.minecraft.world.item.DyeItem> $$2 = new ArrayList<>();
      net.minecraft.world.item.ItemStack $$3 = net.minecraft.world.item.ItemStack.EMPTY;

      for (int $$4 = 0; $$4 < $$0.size(); $$4++) {
         net.minecraft.world.item.ItemStack $$5 = $$0.getItem($$4);
         if (!$$5.isEmpty()) {
            if ($$5.is(ItemTags.DYEABLE)) {
               if (!$$3.isEmpty()) {
                  return net.minecraft.world.item.ItemStack.EMPTY;
               }

               $$3 = $$5.copy();
            } else {
               if (!($$5.getItem() instanceof net.minecraft.world.item.DyeItem $$6)) {
                  return net.minecraft.world.item.ItemStack.EMPTY;
               }

               $$2.add($$6);
            }
         }
      }

      return !$$3.isEmpty() && !$$2.isEmpty() ? DyedItemColor.applyDyes($$3, $$2) : net.minecraft.world.item.ItemStack.EMPTY;
   }

   @Override
   public RecipeSerializer<ArmorDyeRecipe> getSerializer() {
      return RecipeSerializer.ARMOR_DYE;
   }
}
