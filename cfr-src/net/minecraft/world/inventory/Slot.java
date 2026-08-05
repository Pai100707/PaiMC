/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class Slot {
    private final int slot;
    public final Container container;
    public int index;
    public final int x;
    public final int y;

    public Slot(Container $$0, int $$1, int $$2, int $$3) {
        this.container = $$0;
        this.slot = $$1;
        this.x = $$2;
        this.y = $$3;
    }

    public void onQuickCraft(ItemStack $$0, ItemStack $$1) {
        int $$2 = $$1.getCount() - $$0.getCount();
        if ($$2 > 0) {
            this.onQuickCraft($$1, $$2);
        }
    }

    protected void onQuickCraft(ItemStack $$0, int $$1) {
    }

    protected void onSwapCraft(int $$0) {
    }

    protected void checkTakeAchievements(ItemStack $$0) {
    }

    public void onTake(Player $$0, ItemStack $$1) {
        this.setChanged();
    }

    public boolean mayPlace(ItemStack $$0) {
        return true;
    }

    public ItemStack getItem() {
        return this.container.getItem(this.slot);
    }

    public boolean hasItem() {
        return !this.getItem().isEmpty();
    }

    public void setByPlayer(ItemStack $$0) {
        this.setByPlayer($$0, this.getItem());
    }

    public void setByPlayer(ItemStack $$0, ItemStack $$1) {
        this.set($$0);
    }

    public void set(ItemStack $$0) {
        this.container.setItem(this.slot, $$0);
        this.setChanged();
    }

    public void setChanged() {
        this.container.setChanged();
    }

    public int getMaxStackSize() {
        return this.container.getMaxStackSize();
    }

    public int getMaxStackSize(ItemStack $$0) {
        return Math.min(this.getMaxStackSize(), $$0.getMaxStackSize());
    }

    public @Nullable Identifier getNoItemIcon() {
        return null;
    }

    public ItemStack remove(int $$0) {
        return this.container.removeItem(this.slot, $$0);
    }

    public boolean mayPickup(Player $$0) {
        return true;
    }

    public boolean isActive() {
        return true;
    }

    public Optional<ItemStack> tryRemove(int $$0, int $$1, Player $$2) {
        if (!this.mayPickup($$2)) {
            return Optional.empty();
        }
        if (!this.allowModification($$2) && $$1 < this.getItem().getCount()) {
            return Optional.empty();
        }
        ItemStack $$3 = this.remove($$0 = Math.min($$0, $$1));
        if ($$3.isEmpty()) {
            return Optional.empty();
        }
        if (this.getItem().isEmpty()) {
            this.setByPlayer(ItemStack.EMPTY, $$3);
        }
        return Optional.of($$3);
    }

    public ItemStack safeTake(int $$0, int $$12, Player $$2) {
        Optional<ItemStack> $$3 = this.tryRemove($$0, $$12, $$2);
        $$3.ifPresent($$1 -> this.onTake($$2, (ItemStack)$$1));
        return $$3.orElse(ItemStack.EMPTY);
    }

    public ItemStack safeInsert(ItemStack $$0) {
        return this.safeInsert($$0, $$0.getCount());
    }

    public ItemStack safeInsert(ItemStack $$0, int $$1) {
        if ($$0.isEmpty() || !this.mayPlace($$0)) {
            return $$0;
        }
        ItemStack $$2 = this.getItem();
        int $$3 = Math.min(Math.min($$1, $$0.getCount()), this.getMaxStackSize($$0) - $$2.getCount());
        if ($$3 <= 0) {
            return $$0;
        }
        if ($$2.isEmpty()) {
            this.setByPlayer($$0.split($$3));
        } else if (ItemStack.isSameItemSameComponents($$2, $$0)) {
            $$0.shrink($$3);
            $$2.grow($$3);
            this.setByPlayer($$2);
        }
        return $$0;
    }

    public boolean allowModification(Player $$0) {
        return this.mayPickup($$0) && this.mayPlace(this.getItem());
    }

    public int getContainerSlot() {
        return this.slot;
    }

    public boolean isHighlightable() {
        return true;
    }

    public boolean isFake() {
        return false;
    }
}

