/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class NonInteractiveResultSlot
extends Slot {
    public NonInteractiveResultSlot(Container $$0, int $$1, int $$2, int $$3) {
        super($$0, $$1, $$2, $$3);
    }

    @Override
    public void onQuickCraft(ItemStack $$0, ItemStack $$1) {
    }

    @Override
    public boolean mayPickup(Player $$0) {
        return false;
    }

    @Override
    public Optional<ItemStack> tryRemove(int $$0, int $$1, Player $$2) {
        return Optional.empty();
    }

    @Override
    public ItemStack safeTake(int $$0, int $$1, Player $$2) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack safeInsert(ItemStack $$0) {
        return $$0;
    }

    @Override
    public ItemStack safeInsert(ItemStack $$0, int $$1) {
        return this.safeInsert($$0);
    }

    @Override
    public boolean allowModification(Player $$0) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack $$0) {
        return false;
    }

    @Override
    public ItemStack remove(int $$0) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onTake(Player $$0, ItemStack $$1) {
    }

    @Override
    public boolean isHighlightable() {
        return false;
    }

    @Override
    public boolean isFake() {
        return true;
    }
}

