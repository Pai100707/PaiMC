package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

public class BookCloningRecipe extends CustomRecipe {
   public BookCloningRecipe(CraftingBookCategory $$0) {
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
               if ($$5.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
                  if ($$3) {
                     return false;
                  }

                  $$3 = true;
               } else {
                  if (!$$5.is(ItemTags.BOOK_CLONING_TARGET)) {
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
            if ($$5.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
               if (!$$3.isEmpty()) {
                  return net.minecraft.world.item.ItemStack.EMPTY;
               }

               $$3 = $$5;
            } else {
               if (!$$5.is(ItemTags.BOOK_CLONING_TARGET)) {
                  return net.minecraft.world.item.ItemStack.EMPTY;
               }

               $$2++;
            }
         }
      }

      WrittenBookContent $$6 = (WrittenBookContent)$$3.get(DataComponents.WRITTEN_BOOK_CONTENT);
      if (!$$3.isEmpty() && $$2 >= 1 && $$6 != null) {
         WrittenBookContent $$7 = $$6.tryCraftCopy();
         if ($$7 == null) {
            return net.minecraft.world.item.ItemStack.EMPTY;
         } else {
            net.minecraft.world.item.ItemStack $$8 = $$3.copyWithCount($$2);
            $$8.set(DataComponents.WRITTEN_BOOK_CONTENT, $$7);
            return $$8;
         }
      } else {
         return net.minecraft.world.item.ItemStack.EMPTY;
      }
   }

   @Override
   public NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(CraftingInput $$0) {
      NonNullList<net.minecraft.world.item.ItemStack> $$1 = NonNullList.withSize($$0.size(), net.minecraft.world.item.ItemStack.EMPTY);

      for (int $$2 = 0; $$2 < $$1.size(); $$2++) {
         net.minecraft.world.item.ItemStack $$3 = $$0.getItem($$2);
         net.minecraft.world.item.ItemStack $$4 = $$3.getItem().getCraftingRemainder();
         if (!$$4.isEmpty()) {
            $$1.set($$2, $$4);
         } else if ($$3.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            $$1.set($$2, $$3.copyWithCount(1));
            break;
         }
      }

      return $$1;
   }

   @Override
   public RecipeSerializer<BookCloningRecipe> getSerializer() {
      return RecipeSerializer.BOOK_CLONING;
   }
}
