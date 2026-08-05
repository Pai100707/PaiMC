package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;

public class RecipeCache {
   private final RecipeCache.Entry[] entries;
   private WeakReference<RecipeManager> cachedRecipeManager = new WeakReference<>(null);

   public RecipeCache(int $$0) {
      this.entries = new RecipeCache.Entry[$$0];
   }

   public Optional<RecipeHolder<CraftingRecipe>> get(ServerLevel $$0, CraftingInput $$1) {
      if ($$1.isEmpty()) {
         return Optional.empty();
      } else {
         this.validateRecipeManager($$0);

         for (int $$2 = 0; $$2 < this.entries.length; $$2++) {
            RecipeCache.Entry $$3 = this.entries[$$2];
            if ($$3 != null && $$3.matches($$1)) {
               this.moveEntryToFront($$2);
               return Optional.ofNullable($$3.value());
            }
         }

         return this.compute($$1, $$0);
      }
   }

   private void validateRecipeManager(ServerLevel $$0) {
      RecipeManager $$1 = $$0.recipeAccess();
      if ($$1 != this.cachedRecipeManager.get()) {
         this.cachedRecipeManager = new WeakReference<>($$1);
         Arrays.fill(this.entries, null);
      }
   }

   private Optional<RecipeHolder<CraftingRecipe>> compute(CraftingInput $$0, ServerLevel $$1) {
      Optional<RecipeHolder<CraftingRecipe>> $$2 = $$1.recipeAccess().getRecipeFor(RecipeType.CRAFTING, $$0, $$1);
      this.insert($$0, $$2.orElse(null));
      return $$2;
   }

   private void moveEntryToFront(int $$0) {
      if ($$0 > 0) {
         RecipeCache.Entry $$1 = this.entries[$$0];
         System.arraycopy(this.entries, 0, this.entries, 1, $$0);
         this.entries[0] = $$1;
      }
   }

   private void insert(CraftingInput $$0, RecipeHolder<CraftingRecipe> $$1) {
      NonNullList<net.minecraft.world.item.ItemStack> $$2 = NonNullList.withSize($$0.size(), net.minecraft.world.item.ItemStack.EMPTY);

      for (int $$3 = 0; $$3 < $$0.size(); $$3++) {
         $$2.set($$3, $$0.getItem($$3).copyWithCount(1));
      }

      System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
      this.entries[0] = new RecipeCache.Entry($$2, $$0.width(), $$0.height(), $$1);
   }

   record Entry(NonNullList<net.minecraft.world.item.ItemStack> key, int width, int height, RecipeHolder<CraftingRecipe> value) {
      public boolean matches(CraftingInput $$0) {
         if (this.width == $$0.width() && this.height == $$0.height()) {
            for (int $$1 = 0; $$1 < this.key.size(); $$1++) {
               if (!net.minecraft.world.item.ItemStack.isSameItemSameComponents((net.minecraft.world.item.ItemStack)this.key.get($$1), $$0.getItem($$1))) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
