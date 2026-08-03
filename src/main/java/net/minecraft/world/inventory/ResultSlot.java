package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.CraftingInput.Positioned;
import net.minecraft.world.level.Level;

public class ResultSlot extends net.minecraft.world.inventory.Slot {
   private final net.minecraft.world.inventory.CraftingContainer craftSlots;
   private final Player player;
   private int removeCount;

   public ResultSlot(Player $$0, net.minecraft.world.inventory.CraftingContainer $$1, Container $$2, int $$3, int $$4, int $$5) {
      super($$2, $$3, $$4, $$5);
      this.player = $$0;
      this.craftSlots = $$1;
   }

   @Override
   public boolean mayPlace(ItemStack $$0) {
      return false;
   }

   @Override
   public ItemStack remove(int $$0) {
      if (this.hasItem()) {
         this.removeCount = this.removeCount + Math.min($$0, this.getItem().getCount());
      }

      return super.remove($$0);
   }

   @Override
   protected void onQuickCraft(ItemStack $$0, int $$1) {
      this.removeCount += $$1;
      this.checkTakeAchievements($$0);
   }

   @Override
   protected void onSwapCraft(int $$0) {
      this.removeCount += $$0;
   }

   @Override
   protected void checkTakeAchievements(ItemStack $$0) {
      if (this.removeCount > 0) {
         $$0.onCraftedBy(this.player, this.removeCount);
      }

      if (this.container instanceof net.minecraft.world.inventory.RecipeCraftingHolder $$1) {
         $$1.awardUsedRecipes(this.player, this.craftSlots.getItems());
      }

      this.removeCount = 0;
   }

   private static NonNullList<ItemStack> copyAllInputItems(CraftingInput $$0) {
      NonNullList<ItemStack> $$1 = NonNullList.withSize($$0.size(), ItemStack.EMPTY);

      for (int $$2 = 0; $$2 < $$1.size(); $$2++) {
         $$1.set($$2, $$0.getItem($$2));
      }

      return $$1;
   }

   private NonNullList<ItemStack> getRemainingItems(CraftingInput $$0, Level $$1) {
      return $$1 instanceof ServerLevel $$2
         ? $$2.recipeAccess()
            .getRecipeFor(RecipeType.CRAFTING, $$0, $$2)
            .map($$1x -> ((CraftingRecipe)$$1x.value()).getRemainingItems($$0))
            .orElseGet(() -> copyAllInputItems($$0))
         : CraftingRecipe.defaultCraftingReminder($$0);
   }

   @Override
   public void onTake(Player $$0, ItemStack $$1) {
      this.checkTakeAchievements($$1);
      Positioned $$2 = this.craftSlots.asPositionedCraftInput();
      CraftingInput $$3 = $$2.input();
      int $$4 = $$2.left();
      int $$5 = $$2.top();
      NonNullList<ItemStack> $$6 = this.getRemainingItems($$3, $$0.level());

      for (int $$7 = 0; $$7 < $$3.height(); $$7++) {
         for (int $$8 = 0; $$8 < $$3.width(); $$8++) {
            int $$9 = $$8 + $$4 + ($$7 + $$5) * this.craftSlots.getWidth();
            ItemStack $$10 = this.craftSlots.getItem($$9);
            ItemStack $$11 = (ItemStack)$$6.get($$8 + $$7 * $$3.width());
            if (!$$10.isEmpty()) {
               this.craftSlots.removeItem($$9, 1);
               $$10 = this.craftSlots.getItem($$9);
            }

            if (!$$11.isEmpty()) {
               if ($$10.isEmpty()) {
                  this.craftSlots.setItem($$9, $$11);
               } else if (ItemStack.isSameItemSameComponents($$10, $$11)) {
                  $$11.grow($$10.getCount());
                  this.craftSlots.setItem($$9, $$11);
               } else if (!this.player.getInventory().add($$11)) {
                  this.player.drop($$11, false);
               }
            }
         }
      }
   }

   @Override
   public boolean isFake() {
      return true;
   }
}
