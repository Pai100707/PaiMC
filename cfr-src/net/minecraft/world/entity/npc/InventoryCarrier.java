/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.npc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface InventoryCarrier {
    public static final String TAG_INVENTORY = "Inventory";

    public SimpleContainer getInventory();

    public static void pickUpItem(ServerLevel $$0, Mob $$1, InventoryCarrier $$2, ItemEntity $$3) {
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

    default public void readInventoryFromTag(ValueInput $$02) {
        $$02.list(TAG_INVENTORY, ItemStack.CODEC).ifPresent($$0 -> this.getInventory().fromItemList((ValueInput.TypedInputList<ItemStack>)$$0));
    }

    default public void writeInventoryToTag(ValueOutput $$0) {
        this.getInventory().storeAsItemList($$0.list(TAG_INVENTORY, ItemStack.CODEC));
    }
}

