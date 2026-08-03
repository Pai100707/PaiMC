package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FurnaceFuelSlot extends net.minecraft.world.inventory.Slot {
   private final net.minecraft.world.inventory.AbstractFurnaceMenu menu;

   public FurnaceFuelSlot(net.minecraft.world.inventory.AbstractFurnaceMenu $$0, Container $$1, int $$2, int $$3, int $$4) {
      super($$1, $$2, $$3, $$4);
      this.menu = $$0;
   }

   @Override
   public boolean mayPlace(ItemStack $$0) {
      return this.menu.isFuel($$0) || isBucket($$0);
   }

   @Override
   public int getMaxStackSize(ItemStack $$0) {
      return isBucket($$0) ? 1 : super.getMaxStackSize($$0);
   }

   public static boolean isBucket(ItemStack $$0) {
      return $$0.is(Items.BUCKET);
   }
}
