/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.CrafterSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.NonInteractiveResultSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.CrafterBlock;

public class CrafterMenu
extends AbstractContainerMenu
implements ContainerListener {
    protected static final int SLOT_COUNT = 9;
    private static final int INV_SLOT_START = 9;
    private static final int INV_SLOT_END = 36;
    private static final int USE_ROW_SLOT_START = 36;
    private static final int USE_ROW_SLOT_END = 45;
    private final ResultContainer resultContainer = new ResultContainer();
    private final ContainerData containerData;
    private final Player player;
    private final CraftingContainer container;

    public CrafterMenu(int $$0, Inventory $$1) {
        super(MenuType.CRAFTER_3x3, $$0);
        this.player = $$1.player;
        this.containerData = new SimpleContainerData(10);
        this.container = new TransientCraftingContainer(this, 3, 3);
        this.addSlots($$1);
    }

    public CrafterMenu(int $$0, Inventory $$1, CraftingContainer $$2, ContainerData $$3) {
        super(MenuType.CRAFTER_3x3, $$0);
        this.player = $$1.player;
        this.containerData = $$3;
        this.container = $$2;
        CrafterMenu.checkContainerSize($$2, 9);
        $$2.startOpen($$1.player);
        this.addSlots($$1);
        this.addSlotListener(this);
    }

    private void addSlots(Inventory $$0) {
        for (int $$1 = 0; $$1 < 3; ++$$1) {
            for (int $$2 = 0; $$2 < 3; ++$$2) {
                int $$3 = $$2 + $$1 * 3;
                this.addSlot(new CrafterSlot(this.container, $$3, 26 + $$2 * 18, 17 + $$1 * 18, this));
            }
        }
        this.addStandardInventorySlots($$0, 8, 84);
        this.addSlot(new NonInteractiveResultSlot(this.resultContainer, 0, 134, 35));
        this.addDataSlots(this.containerData);
        this.refreshRecipeResult();
    }

    public void setSlotState(int $$0, boolean $$1) {
        CrafterSlot $$2 = (CrafterSlot)this.getSlot($$0);
        this.containerData.set($$2.index, $$1 ? 0 : 1);
        this.broadcastChanges();
    }

    public boolean isSlotDisabled(int $$0) {
        if ($$0 > -1 && $$0 < 9) {
            return this.containerData.get($$0) == 1;
        }
        return false;
    }

    public boolean isPowered() {
        return this.containerData.get(9) == 1;
    }

    @Override
    public ItemStack quickMoveStack(Player $$0, int $$1) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = (Slot)this.slots.get($$1);
        if ($$3 != null && $$3.hasItem()) {
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            if ($$1 < 9 ? !this.moveItemStackTo($$4, 9, 45, true) : !this.moveItemStackTo($$4, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
            if ($$4.isEmpty()) {
                $$3.set(ItemStack.EMPTY);
            } else {
                $$3.setChanged();
            }
            if ($$4.getCount() == $$2.getCount()) {
                return ItemStack.EMPTY;
            }
            $$3.onTake($$0, $$4);
        }
        return $$2;
    }

    @Override
    public boolean stillValid(Player $$0) {
        return this.container.stillValid($$0);
    }

    private void refreshRecipeResult() {
        Player player = this.player;
        if (player instanceof ServerPlayer) {
            ServerPlayer $$0 = (ServerPlayer)player;
            ServerLevel $$1 = $$0.level();
            CraftingInput $$22 = this.container.asCraftInput();
            ItemStack $$3 = CrafterBlock.getPotentialResults($$1, $$22).map($$2 -> ((CraftingRecipe)$$2.value()).assemble($$22, $$1.registryAccess())).orElse(ItemStack.EMPTY);
            this.resultContainer.setItem(0, $$3);
        }
    }

    public Container getContainer() {
        return this.container;
    }

    @Override
    public void slotChanged(AbstractContainerMenu $$0, int $$1, ItemStack $$2) {
        this.refreshRecipeResult();
    }

    @Override
    public void dataChanged(AbstractContainerMenu $$0, int $$1, int $$2) {
    }
}

