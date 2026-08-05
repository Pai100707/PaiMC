/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class ItemCombinerMenu
extends AbstractContainerMenu {
    private static final int INVENTORY_SLOTS_PER_ROW = 9;
    private static final int INVENTORY_ROWS = 3;
    private static final int INPUT_SLOT_START = 0;
    protected final ContainerLevelAccess access;
    protected final Player player;
    protected final Container inputSlots;
    protected final ResultContainer resultSlots = new ResultContainer(){

        @Override
        public void setChanged() {
            ItemCombinerMenu.this.slotsChanged(this);
        }
    };
    private final int resultSlotIndex;

    protected boolean mayPickup(Player $$0, boolean $$1) {
        return true;
    }

    protected abstract void onTake(Player var1, ItemStack var2);

    protected abstract boolean isValidBlock(BlockState var1);

    public ItemCombinerMenu(@Nullable MenuType<?> $$0, int $$1, Inventory $$2, ContainerLevelAccess $$3, ItemCombinerMenuSlotDefinition $$4) {
        super($$0, $$1);
        this.access = $$3;
        this.player = $$2.player;
        this.inputSlots = this.createContainer($$4.getNumOfInputSlots());
        this.resultSlotIndex = $$4.getResultSlotIndex();
        this.createInputSlots($$4);
        this.createResultSlot($$4);
        this.addStandardInventorySlots($$2, 8, 84);
    }

    private void createInputSlots(ItemCombinerMenuSlotDefinition $$0) {
        for (final ItemCombinerMenuSlotDefinition.SlotDefinition $$1 : $$0.getSlots()) {
            this.addSlot(new Slot(this, this.inputSlots, $$1.slotIndex(), $$1.x(), $$1.y()){

                @Override
                public boolean mayPlace(ItemStack $$0) {
                    return $$1.mayPlace().test($$0);
                }
            });
        }
    }

    private void createResultSlot(ItemCombinerMenuSlotDefinition $$0) {
        this.addSlot(new Slot(this.resultSlots, $$0.getResultSlot().slotIndex(), $$0.getResultSlot().x(), $$0.getResultSlot().y()){

            @Override
            public boolean mayPlace(ItemStack $$0) {
                return false;
            }

            @Override
            public boolean mayPickup(Player $$0) {
                return ItemCombinerMenu.this.mayPickup($$0, this.hasItem());
            }

            @Override
            public void onTake(Player $$0, ItemStack $$1) {
                ItemCombinerMenu.this.onTake($$0, $$1);
            }
        });
    }

    public abstract void createResult();

    private SimpleContainer createContainer(int $$0) {
        return new SimpleContainer($$0){

            @Override
            public void setChanged() {
                super.setChanged();
                ItemCombinerMenu.this.slotsChanged(this);
            }
        };
    }

    @Override
    public void slotsChanged(Container $$0) {
        super.slotsChanged($$0);
        if ($$0 == this.inputSlots) {
            this.createResult();
        }
    }

    @Override
    public void removed(Player $$0) {
        super.removed($$0);
        this.access.execute(($$1, $$2) -> this.clearContainer($$0, this.inputSlots));
    }

    @Override
    public boolean stillValid(Player $$0) {
        return this.access.evaluate(($$1, $$2) -> {
            if (!this.isValidBlock($$1.getBlockState((BlockPos)$$2))) {
                return false;
            }
            return $$0.isWithinBlockInteractionRange((BlockPos)$$2, 4.0);
        }, true);
    }

    @Override
    public ItemStack quickMoveStack(Player $$0, int $$1) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = (Slot)this.slots.get($$1);
        if ($$3 != null && $$3.hasItem()) {
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            int $$5 = this.getInventorySlotStart();
            int $$6 = this.getUseRowEnd();
            if ($$1 == this.getResultSlot()) {
                if (!this.moveItemStackTo($$4, $$5, $$6, true)) {
                    return ItemStack.EMPTY;
                }
                $$3.onQuickCraft($$4, $$2);
            } else if ($$1 >= 0 && $$1 < this.getResultSlot() ? !this.moveItemStackTo($$4, $$5, $$6, false) : (this.canMoveIntoInputSlots($$4) && $$1 >= this.getInventorySlotStart() && $$1 < this.getUseRowEnd() ? !this.moveItemStackTo($$4, 0, this.getResultSlot(), false) : ($$1 >= this.getInventorySlotStart() && $$1 < this.getInventorySlotEnd() ? !this.moveItemStackTo($$4, this.getUseRowStart(), this.getUseRowEnd(), false) : $$1 >= this.getUseRowStart() && $$1 < this.getUseRowEnd() && !this.moveItemStackTo($$4, this.getInventorySlotStart(), this.getInventorySlotEnd(), false)))) {
                return ItemStack.EMPTY;
            }
            if ($$4.isEmpty()) {
                $$3.setByPlayer(ItemStack.EMPTY);
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

    protected boolean canMoveIntoInputSlots(ItemStack $$0) {
        return true;
    }

    public int getResultSlot() {
        return this.resultSlotIndex;
    }

    private int getInventorySlotStart() {
        return this.getResultSlot() + 1;
    }

    private int getInventorySlotEnd() {
        return this.getInventorySlotStart() + 27;
    }

    private int getUseRowStart() {
        return this.getInventorySlotEnd();
    }

    private int getUseRowEnd() {
        return this.getUseRowStart() + 9;
    }
}

