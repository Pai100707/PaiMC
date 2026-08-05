/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CrafterSlot
extends Slot {
    private final CrafterMenu menu;

    public CrafterSlot(Container $$0, int $$1, int $$2, int $$3, CrafterMenu $$4) {
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

