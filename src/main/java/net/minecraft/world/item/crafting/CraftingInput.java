package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.StackedItemContents;

public class CraftingInput implements RecipeInput {
   public static final CraftingInput EMPTY = new CraftingInput(0, 0, List.of());
   private final int width;
   private final int height;
   private final List<net.minecraft.world.item.ItemStack> items;
   private final StackedItemContents stackedContents = new StackedItemContents();
   private final int ingredientCount;

   private CraftingInput(int $$0, int $$1, List<net.minecraft.world.item.ItemStack> $$2) {
      this.width = $$0;
      this.height = $$1;
      this.items = $$2;
      int $$3 = 0;

      for (net.minecraft.world.item.ItemStack $$4 : $$2) {
         if (!$$4.isEmpty()) {
            $$3++;
            this.stackedContents.accountStack($$4, 1);
         }
      }

      this.ingredientCount = $$3;
   }

   public static CraftingInput of(int $$0, int $$1, List<net.minecraft.world.item.ItemStack> $$2) {
      return ofPositioned($$0, $$1, $$2).input();
   }

   public static CraftingInput.Positioned ofPositioned(int $$0, int $$1, List<net.minecraft.world.item.ItemStack> $$2) {
      if ($$0 != 0 && $$1 != 0) {
         int $$3 = $$0 - 1;
         int $$4 = 0;
         int $$5 = $$1 - 1;
         int $$6 = 0;

         for (int $$7 = 0; $$7 < $$1; $$7++) {
            boolean $$8 = true;

            for (int $$9 = 0; $$9 < $$0; $$9++) {
               net.minecraft.world.item.ItemStack $$10 = $$2.get($$9 + $$7 * $$0);
               if (!$$10.isEmpty()) {
                  $$3 = Math.min($$3, $$9);
                  $$4 = Math.max($$4, $$9);
                  $$8 = false;
               }
            }

            if (!$$8) {
               $$5 = Math.min($$5, $$7);
               $$6 = Math.max($$6, $$7);
            }
         }

         int $$11 = $$4 - $$3 + 1;
         int $$12 = $$6 - $$5 + 1;
         if ($$11 <= 0 || $$12 <= 0) {
            return CraftingInput.Positioned.EMPTY;
         } else if ($$11 == $$0 && $$12 == $$1) {
            return new CraftingInput.Positioned(new CraftingInput($$0, $$1, $$2), $$3, $$5);
         } else {
            List<net.minecraft.world.item.ItemStack> $$13 = new ArrayList<>($$11 * $$12);

            for (int $$14 = 0; $$14 < $$12; $$14++) {
               for (int $$15 = 0; $$15 < $$11; $$15++) {
                  int $$16 = $$15 + $$3 + ($$14 + $$5) * $$0;
                  $$13.add($$2.get($$16));
               }
            }

            return new CraftingInput.Positioned(new CraftingInput($$11, $$12, $$13), $$3, $$5);
         }
      } else {
         return CraftingInput.Positioned.EMPTY;
      }
   }

   @Override
   public net.minecraft.world.item.ItemStack getItem(int $$0) {
      return this.items.get($$0);
   }

   public net.minecraft.world.item.ItemStack getItem(int $$0, int $$1) {
      return this.items.get($$0 + $$1 * this.width);
   }

   @Override
   public int size() {
      return this.items.size();
   }

   @Override
   public boolean isEmpty() {
      return this.ingredientCount == 0;
   }

   public StackedItemContents stackedContents() {
      return this.stackedContents;
   }

   public List<net.minecraft.world.item.ItemStack> items() {
      return this.items;
   }

   public int ingredientCount() {
      return this.ingredientCount;
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }

   @Override
   public boolean equals(Object $$0) {
      if ($$0 == this) {
         return true;
      } else {
         return !($$0 instanceof CraftingInput $$1)
            ? false
            : this.width == $$1.width
               && this.height == $$1.height
               && this.ingredientCount == $$1.ingredientCount
               && net.minecraft.world.item.ItemStack.listMatches(this.items, $$1.items);
      }
   }

   @Override
   public int hashCode() {
      int $$0 = net.minecraft.world.item.ItemStack.hashStackList(this.items);
      $$0 = 31 * $$0 + this.width;
      return 31 * $$0 + this.height;
   }

   public record Positioned(CraftingInput input, int left, int top) {
      public static final CraftingInput.Positioned EMPTY = new CraftingInput.Positioned(CraftingInput.EMPTY, 0, 0);
   }
}
