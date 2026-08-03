package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.recipebook.ServerPlaceRecipe.CraftingMenuAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class AbstractCraftingMenu extends net.minecraft.world.inventory.RecipeBookMenu {
   private final int width;
   private final int height;
   protected final net.minecraft.world.inventory.CraftingContainer craftSlots;
   protected final net.minecraft.world.inventory.ResultContainer resultSlots = new net.minecraft.world.inventory.ResultContainer();

   public AbstractCraftingMenu(net.minecraft.world.inventory.MenuType<?> $$0, int $$1, int $$2, int $$3) {
      super($$0, $$1);
      this.width = $$2;
      this.height = $$3;
      this.craftSlots = new net.minecraft.world.inventory.TransientCraftingContainer(this, $$2, $$3);
   }

   protected net.minecraft.world.inventory.Slot addResultSlot(Player $$0, int $$1, int $$2) {
      return this.addSlot(new net.minecraft.world.inventory.ResultSlot($$0, this.craftSlots, this.resultSlots, 0, $$1, $$2));
   }

   protected void addCraftingGridSlots(int $$0, int $$1) {
      for (int $$2 = 0; $$2 < this.width; $$2++) {
         for (int $$3 = 0; $$3 < this.height; $$3++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(this.craftSlots, $$3 + $$2 * this.width, $$0 + $$3 * 18, $$1 + $$2 * 18));
         }
      }
   }

   @Override
   public net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction handlePlacement(
      boolean $$0, boolean $$1, RecipeHolder<?> $$2, ServerLevel $$3, Inventory $$4
   ) {
      RecipeHolder<CraftingRecipe> $$5 = (RecipeHolder<CraftingRecipe>)$$2;
      this.beginPlacingRecipe();

      net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction var8;
      try {
         List<net.minecraft.world.inventory.Slot> $$6 = this.getInputGridSlots();
         var8 = ServerPlaceRecipe.placeRecipe(new CraftingMenuAccess<CraftingRecipe>() {
            public void fillCraftSlotsStackedContents(StackedItemContents $$0) {
               AbstractCraftingMenu.this.fillCraftSlotsStackedContents($$0);
            }

            public void clearCraftingContent() {
               AbstractCraftingMenu.this.resultSlots.clearContent();
               AbstractCraftingMenu.this.craftSlots.clearContent();
            }

            public boolean recipeMatches(RecipeHolder<CraftingRecipe> $$0) {
               return ((CraftingRecipe)$$0.value()).matches(AbstractCraftingMenu.this.craftSlots.asCraftInput(), AbstractCraftingMenu.this.owner().level());
            }
         }, this.width, this.height, $$6, $$6, $$4, $$5, $$0, $$1);
      } finally {
         this.finishPlacingRecipe($$3, (RecipeHolder<CraftingRecipe>)$$2);
      }

      return var8;
   }

   protected void beginPlacingRecipe() {
   }

   protected void finishPlacingRecipe(ServerLevel $$0, RecipeHolder<CraftingRecipe> $$1) {
   }

   public abstract net.minecraft.world.inventory.Slot getResultSlot();

   public abstract List<net.minecraft.world.inventory.Slot> getInputGridSlots();

   public int getGridWidth() {
      return this.width;
   }

   public int getGridHeight() {
      return this.height;
   }

   protected abstract Player owner();

   @Override
   public void fillCraftSlotsStackedContents(StackedItemContents $$0) {
      this.craftSlots.fillStackedContents($$0);
   }
}
