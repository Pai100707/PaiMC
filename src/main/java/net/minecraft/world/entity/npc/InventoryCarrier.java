package net.minecraft.world.entity.npc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface InventoryCarrier {
   String TAG_INVENTORY = "Inventory";

   SimpleContainer getInventory();

   static void pickUpItem(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, InventoryCarrier $$2, ItemEntity $$3) {
      ItemStack $$4 = $$3.getItem();
      if ($$1.wantsToPickUp($$0, $$4)) {
         SimpleContainer $$5 = $$2.getInventory();
         boolean $$6 = $$5.canAddItem($$4);
         if (!$$6) {
            return;
         }

         $$1.onItemPickup($$3);
         int $$7 = $$4.getCount();
         ItemStack $$8 = $$5.addItem($$4);
         $$1.take($$3, $$7 - $$8.getCount());
         if ($$8.isEmpty()) {
            $$3.discard();
         } else {
            $$4.setCount($$8.getCount());
         }
      }
   }

   default void readInventoryFromTag(ValueInput $$0) {
      $$0.list("Inventory", ItemStack.CODEC).ifPresent($$0x -> this.getInventory().fromItemList($$0x));
   }

   default void writeInventoryToTag(ValueOutput $$0) {
      this.getInventory().storeAsItemList($$0.list("Inventory", ItemStack.CODEC));
   }
}
