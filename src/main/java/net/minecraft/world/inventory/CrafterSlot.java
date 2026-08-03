package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class CrafterSlot extends net.minecraft.world.inventory.Slot {
   private final net.minecraft.world.inventory.CrafterMenu menu;

   public CrafterSlot(Container $$0, int $$1, int $$2, int $$3, net.minecraft.world.inventory.CrafterMenu $$4) {
      super($$0, $$1, $$2, $$3);
      this.menu = $$4;
   }

   @Override
   public boolean mayPlace(ItemStack $$0) {
      return !this.menu.isSlotDisabled(this.index) && super.mayPlace($$0);
   }

   @Override
   public void setChanged() {
      super.setChanged();
      this.menu.slotsChanged(this.container);
   }
}
