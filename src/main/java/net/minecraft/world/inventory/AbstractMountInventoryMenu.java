package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractMountInventoryMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
   protected final Container mountContainer;
   protected final LivingEntity mount;
   protected final int SLOT_SADDLE = 0;
   protected final int SLOT_BODY_ARMOR = 1;
   protected final int SLOT_INVENTORY_START = 2;
   protected static final int INVENTORY_ROWS = 3;

   protected AbstractMountInventoryMenu(int $$0, Inventory $$1, Container $$2, LivingEntity $$3) {
      super(null, $$0);
      this.mountContainer = $$2;
      this.mount = $$3;
      $$2.startOpen($$1.player);
   }

   protected abstract boolean hasInventoryChanged(Container var1);

   @Override
   public boolean stillValid(Player $$0) {
      return !this.hasInventoryChanged(this.mountContainer)
         && this.mountContainer.stillValid($$0)
         && this.mount.isAlive()
         && $$0.isWithinEntityInteractionRange(this.mount, 4.0);
   }

   @Override
   public void removed(Player $$0) {
      super.removed($$0);
      this.mountContainer.stopOpen($$0);
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
      if ($$3 != null && $$3.hasItem()) {
         ItemStack $$4 = $$3.getItem();
         $$2 = $$4.copy();
         int $$5 = 2 + this.mountContainer.getContainerSize();
         if ($$1 < $$5) {
            if (!this.moveItemStackTo($$4, $$5, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(1).mayPlace($$4) && !this.getSlot(1).hasItem()) {
            if (!this.moveItemStackTo($$4, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(0).mayPlace($$4) && !this.getSlot(0).hasItem()) {
            if (!this.moveItemStackTo($$4, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.mountContainer.getContainerSize() == 0 || !this.moveItemStackTo($$4, 2, $$5, false)) {
            int $$6 = $$5 + 27;
            int $$8 = $$6 + 9;
            if ($$1 >= $$6 && $$1 < $$8) {
               if (!this.moveItemStackTo($$4, $$5, $$6, false)) {
                  return ItemStack.EMPTY;
               }
            } else if ($$1 >= $$5 && $$1 < $$6) {
               if (!this.moveItemStackTo($$4, $$6, $$8, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo($$4, $$6, $$6, false)) {
               return ItemStack.EMPTY;
            }

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

   public static int getInventorySize(int $$0) {
      return $$0 * 3;
   }
}
