package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;

public class SmokerMenu extends net.minecraft.world.inventory.AbstractFurnaceMenu {
   public SmokerMenu(int $$0, Inventory $$1) {
      super(
         net.minecraft.world.inventory.MenuType.SMOKER,
         RecipeType.SMOKING,
         RecipePropertySet.SMOKER_INPUT,
         net.minecraft.world.inventory.RecipeBookType.SMOKER,
         $$0,
         $$1
      );
   }

   public SmokerMenu(int $$0, Inventory $$1, Container $$2, net.minecraft.world.inventory.ContainerData $$3) {
      super(
         net.minecraft.world.inventory.MenuType.SMOKER,
         RecipeType.SMOKING,
         RecipePropertySet.SMOKER_INPUT,
         net.minecraft.world.inventory.RecipeBookType.SMOKER,
         $$0,
         $$1,
         $$2,
         $$3
      );
   }
}
