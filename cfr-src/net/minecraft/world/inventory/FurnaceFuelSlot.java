/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FurnaceFuelSlot
extends Slot {
    private final AbstractFurnaceMenu menu;

    public FurnaceFuelSlot(AbstractFurnaceMenu $$0, Container $$1, int $$2, int $$3, int $$4) {
        super($$1, $$2, $$3, $$4);
        this.menu = $$0;
    }

    @Override
    public boolean mayPlace(ItemStack $$0) {
        return this.menu.isFuel($$0) || FurnaceFuelSlot.isBucket($$0);
    }

    @Override
    public int getMaxStackSize(ItemStack $$0) {
        return FurnaceFuelSlot.isBucket($$0) ? 1 : super.getMaxStackSize($$0);
    }

    public static boolean isBucket(ItemStack $$0) {
        return $$0.is(Items.BUCKET);
    }
}

