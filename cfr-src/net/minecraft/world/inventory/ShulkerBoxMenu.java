/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxMenu
extends AbstractContainerMenu {
    private static final int CONTAINER_SIZE = 27;
    private final Container container;

    public ShulkerBoxMenu(int $$0, Inventory $$1) {
        this($$0, $$1, new SimpleContainer(27));
    }

    public ShulkerBoxMenu(int $$0, Inventory $$1, Container $$2) {
        super(MenuType.SHULKER_BOX, $$0);
        ShulkerBoxMenu.checkContainerSize($$2, 27);
        this.container = $$2;
        $$2.startOpen($$1.player);
        int $$3 = 3;
        int $$4 = 9;
        for (int $$5 = 0; $$5 < 3; ++$$5) {
            for (int $$6 = 0; $$6 < 9; ++$$6) {
                this.addSlot(new ShulkerBoxSlot($$2, $$6 + $$5 * 9, 8 + $$6 * 18, 18 + $$5 * 18));
            }
        }
        this.addStandardInventorySlots($$1, 8, 84);
    }

    @Override
    public boolean stillValid(Player $$0) {
        return this.container.stillValid($$0);
    }

    @Override
    public ItemStack quickMoveStack(Player $$0, int $$1) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = (Slot)this.slots.get($$1);
        if ($$3 != null && $$3.hasItem()) {
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            if ($$1 < this.container.getContainerSize() ? !this.moveItemStackTo($$4, this.container.getContainerSize(), this.slots.size(), true) : !this.moveItemStackTo($$4, 0, this.container.getContainerSize(), false)) {
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
        this.container.stopOpen($$0);
    }
}

