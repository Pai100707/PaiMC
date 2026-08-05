/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public class TransientCraftingContainer
implements CraftingContainer {
    private final NonNullList<ItemStack> items;
    private final int width;
    private final int height;
    private final AbstractContainerMenu menu;

    public TransientCraftingContainer(AbstractContainerMenu $$0, int $$1, int $$2) {
        this($$0, $$1, $$2, NonNullList.withSize($$1 * $$2, ItemStack.EMPTY));
    }

    private TransientCraftingContainer(AbstractContainerMenu $$0, int $$1, int $$2, NonNullList<ItemStack> $$3) {
        this.items = $$3;
        this.menu = $$0;
        this.width = $$1;
        this.height = $$2;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack $$0 : this.items) {
            if ($$0.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int $$0) {
        if ($$0 >= this.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return this.items.get($$0);
    }

    @Override
    public ItemStack removeItemNoUpdate(int $$0) {
        return ContainerHelper.takeItem(this.items, $$0);
    }

    @Override
    public ItemStack removeItem(int $$0, int $$1) {
        ItemStack $$2 = ContainerHelper.removeItem(this.items, $$0, $$1);
        if (!$$2.isEmpty()) {
            this.menu.slotsChanged(this);
        }
        return $$2;
    }

    @Override
    public void setItem(int $$0, ItemStack $$1) {
        this.items.set($$0, $$1);
        this.menu.slotsChanged(this);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player $$0) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public List<ItemStack> getItems() {
        return List.copyOf(this.items);
    }

    @Override
    public void fillStackedContents(StackedItemContents $$0) {
        for (ItemStack $$1 : this.items) {
            $$0.accountSimpleStack($$1);
        }
    }
}

