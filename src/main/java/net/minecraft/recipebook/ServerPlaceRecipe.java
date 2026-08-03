package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class ServerPlaceRecipe<R extends Recipe<?>> {
   private static final int ITEM_NOT_FOUND = -1;
   private final Inventory inventory;
   private final net.minecraft.recipebook.ServerPlaceRecipe.CraftingMenuAccess<R> menu;
   private final boolean useMaxItems;
   private final int gridWidth;
   private final int gridHeight;
   private final List<Slot> inputGridSlots;
   private final List<Slot> slotsToClear;

   public static <I extends RecipeInput, R extends Recipe<I>> PostPlaceAction placeRecipe(
      net.minecraft.recipebook.ServerPlaceRecipe.CraftingMenuAccess<R> $$0,
      int $$1,
      int $$2,
      List<Slot> $$3,
      List<Slot> $$4,
      Inventory $$5,
      RecipeHolder<R> $$6,
      boolean $$7,
      boolean $$8
   ) {
      net.minecraft.recipebook.ServerPlaceRecipe<R> $$9 = new net.minecraft.recipebook.ServerPlaceRecipe<>($$0, $$5, $$7, $$1, $$2, $$3, $$4);
      if (!$$8 && !$$9.testClearGrid()) {
         return PostPlaceAction.NOTHING;
      } else {
         StackedItemContents $$10 = new StackedItemContents();
         $$5.fillStackedContents($$10);
         $$0.fillCraftSlotsStackedContents($$10);
         return $$9.tryPlaceRecipe($$6, $$10);
      }
   }

   private ServerPlaceRecipe(
      net.minecraft.recipebook.ServerPlaceRecipe.CraftingMenuAccess<R> $$0, Inventory $$1, boolean $$2, int $$3, int $$4, List<Slot> $$5, List<Slot> $$6
   ) {
      this.menu = $$0;
      this.inventory = $$1;
      this.useMaxItems = $$2;
      this.gridWidth = $$3;
      this.gridHeight = $$4;
      this.inputGridSlots = $$5;
      this.slotsToClear = $$6;
   }

   private PostPlaceAction tryPlaceRecipe(RecipeHolder<R> $$0, StackedItemContents $$1) {
      if ($$1.canCraft($$0.value(), null)) {
         this.placeRecipe($$0, $$1);
         this.inventory.setChanged();
         return PostPlaceAction.NOTHING;
      } else {
         this.clearGrid();
         this.inventory.setChanged();
         return PostPlaceAction.PLACE_GHOST_RECIPE;
      }
   }

   private void clearGrid() {
      for (Slot $$0 : this.slotsToClear) {
         ItemStack $$1 = $$0.getItem().copy();
         this.inventory.placeItemBackInInventory($$1, false);
         $$0.set($$1);
      }

      this.menu.clearCraftingContent();
   }

   private void placeRecipe(RecipeHolder<R> $$0, StackedItemContents $$1) {
      boolean $$2 = this.menu.recipeMatches($$0);
      int $$3 = $$1.getBiggestCraftableStack($$0.value(), null);
      if ($$2) {
         for (Slot $$4 : this.inputGridSlots) {
            ItemStack $$5 = $$4.getItem();
            if (!$$5.isEmpty() && Math.min($$3, $$5.getMaxStackSize()) < $$5.getCount() + 1) {
               return;
            }
         }
      }

      int $$6 = this.calculateAmountToCraft($$3, $$2);
      List<Holder<Item>> $$7 = new ArrayList<>();
      if ($$1.canCraft($$0.value(), $$6, $$7::add)) {
         int $$8 = clampToMaxStackSize($$6, $$7);
         if ($$8 != $$6) {
            $$7.clear();
            if (!$$1.canCraft($$0.value(), $$8, $$7::add)) {
               return;
            }
         }

         this.clearGrid();
         net.minecraft.recipebook.PlaceRecipeHelper.placeRecipe(
            this.gridWidth, this.gridHeight, $$0.value(), $$0.value().placementInfo().slotsToIngredientIndex(), ($$2x, $$3x, $$4x, $$5) -> {
               if ($$2x != -1) {
                  Slot $$6x = this.inputGridSlots.get($$3x);
                  Holder<Item> $$7x = $$7.get($$2x);
                  int $$8x = $$8;

                  while ($$8x > 0) {
                     $$8x = this.moveItemToGrid($$6x, $$7x, $$8x);
                     if ($$8x == -1) {
                        return;
                     }
                  }
               }
            }
         );
      }
   }

   private static int clampToMaxStackSize(int $$0, List<Holder<Item>> $$1) {
      for (Holder<Item> $$2 : $$1) {
         $$0 = Math.min($$0, ((Item)$$2.value()).getDefaultMaxStackSize());
      }

      return $$0;
   }

   private int calculateAmountToCraft(int $$0, boolean $$1) {
      if (this.useMaxItems) {
         return $$0;
      } else if ($$1) {
         int $$2 = Integer.MAX_VALUE;

         for (Slot $$3 : this.inputGridSlots) {
            ItemStack $$4 = $$3.getItem();
            if (!$$4.isEmpty() && $$2 > $$4.getCount()) {
               $$2 = $$4.getCount();
            }
         }

         if ($$2 != Integer.MAX_VALUE) {
            $$2++;
         }

         return $$2;
      } else {
         return 1;
      }
   }

   private int moveItemToGrid(Slot $$0, Holder<Item> $$1, int $$2) {
      ItemStack $$3 = $$0.getItem();
      int $$4 = this.inventory.findSlotMatchingCraftingIngredient($$1, $$3);
      if ($$4 == -1) {
         return -1;
      } else {
         ItemStack $$5 = this.inventory.getItem($$4);
         ItemStack $$6;
         if ($$2 < $$5.getCount()) {
            $$6 = this.inventory.removeItem($$4, $$2);
         } else {
            $$6 = this.inventory.removeItemNoUpdate($$4);
         }

         int $$8 = $$6.getCount();
         if ($$3.isEmpty()) {
            $$0.set($$6);
         } else {
            $$3.grow($$8);
         }

         return $$2 - $$8;
      }
   }

   private boolean testClearGrid() {
      List<ItemStack> $$0 = Lists.newArrayList();
      int $$1 = this.getAmountOfFreeSlotsInInventory();

      for (Slot $$2 : this.inputGridSlots) {
         ItemStack $$3 = $$2.getItem().copy();
         if (!$$3.isEmpty()) {
            int $$4 = this.inventory.getSlotWithRemainingSpace($$3);
            if ($$4 == -1 && $$0.size() <= $$1) {
               for (ItemStack $$5 : $$0) {
                  if (ItemStack.isSameItem($$5, $$3) && $$5.getCount() != $$5.getMaxStackSize() && $$5.getCount() + $$3.getCount() <= $$5.getMaxStackSize()) {
                     $$5.grow($$3.getCount());
                     $$3.setCount(0);
                     break;
                  }
               }

               if (!$$3.isEmpty()) {
                  if ($$0.size() >= $$1) {
                     return false;
                  }

                  $$0.add($$3);
               }
            } else if ($$4 == -1) {
               return false;
            }
         }
      }

      return true;
   }

   private int getAmountOfFreeSlotsInInventory() {
      int $$0 = 0;

      for (ItemStack $$1 : this.inventory.getNonEquipmentItems()) {
         if ($$1.isEmpty()) {
            $$0++;
         }
      }

      return $$0;
   }

   public interface CraftingMenuAccess<T extends Recipe<?>> {
      void fillCraftSlotsStackedContents(StackedItemContents var1);

      void clearCraftingContent();

      boolean recipeMatches(RecipeHolder<T> var1);
   }
}
