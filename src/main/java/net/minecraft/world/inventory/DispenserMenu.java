package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DispenserMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
   private static final int SLOT_COUNT = 9;
   private static final int INV_SLOT_START = 9;
   private static final int INV_SLOT_END = 36;
   private static final int USE_ROW_SLOT_START = 36;
   private static final int USE_ROW_SLOT_END = 45;
   private final Container dispenser;

   public DispenserMenu(int $$0, Inventory $$1) {
      this($$0, $$1, new SimpleContainer(9));
   }

   public DispenserMenu(int $$0, Inventory $$1, Container $$2) {
      super(net.minecraft.world.inventory.MenuType.GENERIC_3x3, $$0);
      checkContainerSize($$2, 9);
      this.dispenser = $$2;
      $$2.startOpen($$1.player);
      this.add3x3GridSlots($$2, 62, 17);
      this.addStandardInventorySlots($$1, 8, 84);
   }

   protected void add3x3GridSlots(Container $$0, int $$1, int $$2) {
      for (int $$3 = 0; $$3 < 3; $$3++) {
         for (int $$4 = 0; $$4 < 3; $$4++) {
            int $$5 = $$4 + $$3 * 3;
            this.addSlot(new net.minecraft.world.inventory.Slot($$0, $$5, $$1 + $$4 * 18, $$2 + $$3 * 18));
         }
      }
   }

   @Override
   public boolean stillValid(Player $$0) {
      return this.dispenser.stillValid($$0);
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
      if ($$3 != null && $$3.hasItem()) {
         ItemStack $$4 = $$3.getItem();
         $$2 = $$4.copy();
         if ($$1 < 9) {
            if (!this.moveItemStackTo($$4, 9, 45, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo($$4, 0, 9, false)) {
            return ItemStack.EMPTY;
         }

         if ($$4.isEmpty()) {
            $$3.setByPlayer(ItemStack.EMPTY);
         } else {
            $$3.setChanged();
         }

         if ($$4.getCount() == $$2.getCount()) {
            return ItemStack.EMPTY;
         }

         $$3.onTake($$0, $$4);
      }

      return $$2;
   }

   @Override
   public void removed(Player $$0) {
      super.removed($$0);
      this.dispenser.stopOpen($$0);
   }
}
