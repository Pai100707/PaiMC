package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HopperMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
   public static final int CONTAINER_SIZE = 5;
   private final Container hopper;

   public HopperMenu(int $$0, Inventory $$1) {
      this($$0, $$1, new SimpleContainer(5));
   }

   public HopperMenu(int $$0, Inventory $$1, Container $$2) {
      super(net.minecraft.world.inventory.MenuType.HOPPER, $$0);
      this.hopper = $$2;
      checkContainerSize($$2, 5);
      $$2.startOpen($$1.player);

      for (int $$3 = 0; $$3 < 5; $$3++) {
         this.addSlot(new net.minecraft.world.inventory.Slot($$2, $$3, 44 + $$3 * 18, 20));
      }

      this.addStandardInventorySlots($$1, 8, 51);
   }

   @Override
   public boolean stillValid(Player $$0) {
      return this.hopper.stillValid($$0);
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
      if ($$3 != null && $$3.hasItem()) {
         ItemStack $$4 = $$3.getItem();
         $$2 = $$4.copy();
         if ($$1 < this.hopper.getContainerSize()) {
            if (!this.moveItemStackTo($$4, this.hopper.getContainerSize(), this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo($$4, 0, this.hopper.getContainerSize(), false)) {
            return ItemStack.EMPTY;
         }

         if ($$4.isEmpty()) {
            $$3.setByPlayer(ItemStack.EMPTY);
         } else {
            $$3.setChanged();
         }
      }

      return $$2;
   }

   @Override
   public void removed(Player $$0) {
      super.removed($$0);
      this.hopper.stopOpen($$0);
   }
}
