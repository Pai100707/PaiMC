package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class RecipeBookMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
   public RecipeBookMenu(net.minecraft.world.inventory.MenuType<?> $$0, int $$1) {
      super($$0, $$1);
   }

   public abstract net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction handlePlacement(
      boolean var1, boolean var2, RecipeHolder<?> var3, ServerLevel var4, Inventory var5
   );

   public abstract void fillCraftSlotsStackedContents(StackedItemContents var1);

   public abstract net.minecraft.world.inventory.RecipeBookType getRecipeBookType();

   public static enum PostPlaceAction {
      NOTHING,
      PLACE_GHOST_RECIPE;
   }
}
