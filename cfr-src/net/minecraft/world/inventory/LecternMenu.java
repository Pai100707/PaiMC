/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LecternMenu
extends AbstractContainerMenu {
    private static final int DATA_COUNT = 1;
    private static final int SLOT_COUNT = 1;
    public static final int BUTTON_PREV_PAGE = 1;
    public static final int BUTTON_NEXT_PAGE = 2;
    public static final int BUTTON_TAKE_BOOK = 3;
    public static final int BUTTON_PAGE_JUMP_RANGE_START = 100;
    private final Container lectern;
    private final ContainerData lecternData;

    public LecternMenu(int $$0) {
        this($$0, new SimpleContainer(1), new SimpleContainerData(1));
    }

    public LecternMenu(int $$0, Container $$1, ContainerData $$2) {
        super(MenuType.LECTERN, $$0);
        LecternMenu.checkContainerSize($$1, 1);
        LecternMenu.checkContainerDataCount($$2, 1);
        this.lectern = $$1;
        this.lecternData = $$2;
        this.addSlot(new Slot($$1, 0, 0, 0){

            @Override
            public void setChanged() {
                super.setChanged();
                LecternMenu.this.slotsChanged(this.container);
            }
        });
        this.addDataSlots($$2);
    }

    @Override
    public boolean clickMenuButton(Player $$0, int $$1) {
        if ($$1 >= 100) {
            int $$2 = $$1 - 100;
            this.setData(0, $$2);
            return true;
        }
        switch ($$1) {
            case 2: {
                int $$3 = this.lecternData.get(0);
                this.setData(0, $$3 + 1);
                return true;
            }
            case 1: {
                int $$4 = this.lecternData.get(0);
                this.setData(0, $$4 - 1);
                return true;
            }
            case 3: {
                if (!$$0.mayBuild()) {
                    return false;
                }
                ItemStack $$5 = this.lectern.removeItemNoUpdate(0);
                this.lectern.setChanged();
                if (!$$0.getInventory().add($$5)) {
                    $$0.drop($$5, false);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player $$0, int $$1) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setData(int $$0, int $$1) {
        super.setData($$0, $$1);
        this.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player $$0) {
        return this.lectern.stillValid($$0);
    }

    public ItemStack getBook() {
        return this.lectern.getItem(0);
    }

    public int getPage() {
        return this.lecternData.get(0);
    }
}

