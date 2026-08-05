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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ChestMenu
extends AbstractContainerMenu {
    private final Container container;
    private final int containerRows;

    private ChestMenu(MenuType<?> $$0, int $$1, Inventory $$2, int $$3) {
        this($$0, $$1, $$2, new SimpleContainer(9 * $$3), $$3);
    }

    public static ChestMenu oneRow(int $$0, Inventory $$1) {
        return new ChestMenu(MenuType.GENERIC_9x1, $$0, $$1, 1);
    }

    public static ChestMenu twoRows(int $$0, Inventory $$1) {
        return new ChestMenu(MenuType.GENERIC_9x2, $$0, $$1, 2);
    }

    public static ChestMenu threeRows(int $$0, Inventory $$1) {
        return new ChestMenu(MenuType.GENERIC_9x3, $$0, $$1, 3);
    }

    public static ChestMenu fourRows(int $$0, Inventory $$1) {
        return new ChestMenu(MenuType.GENERIC_9x4, $$0, $$1, 4);
    }

    public static ChestMenu fiveRows(int $$0, Inventory $$1) {
        return new ChestMenu(MenuType.GENERIC_9x5, $$0, $$1, 5);
    }

    public static ChestMenu sixRows(int $$0, Inventory $$1) {
        return new ChestMenu(MenuType.GENERIC_9x6, $$0, $$1, 6);
    }

    public static ChestMenu threeRows(int $$0, Inventory $$1, Container $$2) {
        return new ChestMenu(MenuType.GENERIC_9x3, $$0, $$1, $$2, 3);
    }

    public static ChestMenu sixRows(int $$0, Inventory $$1, Container $$2) {
        return new ChestMenu(MenuType.GENERIC_9x6, $$0, $$1, $$2, 6);
    }

    public ChestMenu(MenuType<?> $$0, int $$1, Inventory $$2, Container $$3, int $$4) {
        super($$0, $$1);
        ChestMenu.checkContainerSize($$3, $$4 * 9);
        this.container = $$3;
        this.containerRows = $$4;
        $$3.startOpen($$2.player);
        int $$5 = 18;
        this.addChestGrid($$3, 8, 18);
        int $$6 = 18 + this.containerRows * 18 + 13;
        this.addStandardInventorySlots($$2, 8, $$6);
    }

    private void addChestGrid(Container $$0, int $$1, int $$2) {
        for (int $$3 = 0; $$3 < this.containerRows; ++$$3) {
            for (int $$4 = 0; $$4 < 9; ++$$4) {
                this.addSlot(new Slot($$0, $$4 + $$3 * 9, $$1 + $$4 * 18, $$2 + $$3 * 18));
            }
        }
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
            if ($$1 < this.containerRows * 9 ? !this.moveItemStackTo($$4, this.containerRows * 9, this.slots.size(), true) : !this.moveItemStackTo($$4, 0, this.containerRows * 9, false)) {
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

    public Container getContainer() {
        return this.container;
    }

    public int getRowCount() {
        return this.containerRows;
    }
}

