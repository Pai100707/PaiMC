/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class PlayerEnderChestContainer
extends SimpleContainer {
    private @Nullable EnderChestBlockEntity activeChest;

    public PlayerEnderChestContainer() {
        super(27);
    }

    public void setActiveChest(EnderChestBlockEntity $$0) {
        this.activeChest = $$0;
    }

    public boolean isActiveChest(EnderChestBlockEntity $$0) {
        return this.activeChest == $$0;
    }

    public void fromSlots(ValueInput.TypedInputList<ItemStackWithSlot> $$0) {
        for (int $$1 = 0; $$1 < this.getContainerSize(); ++$$1) {
            this.setItem($$1, ItemStack.EMPTY);
        }
        for (ItemStackWithSlot $$2 : $$0) {
            if (!$$2.isValidInContainer(this.getContainerSize())) continue;
            this.setItem($$2.slot(), $$2.stack());
        }
    }

    public void storeAsSlots(ValueOutput.TypedOutputList<ItemStackWithSlot> $$0) {
        for (int $$1 = 0; $$1 < this.getContainerSize(); ++$$1) {
            ItemStack $$2 = this.getItem($$1);
            if ($$2.isEmpty()) continue;
            $$0.add(new ItemStackWithSlot($$1, $$2));
        }
    }

    @Override
    public boolean stillValid(Player $$0) {
        if (this.activeChest != null && !this.activeChest.stillValid($$0)) {
            return false;
        }
        return super.stillValid($$0);
    }

    @Override
    public void startOpen(ContainerUser $$0) {
        if (this.activeChest != null) {
            this.activeChest.startOpen($$0);
        }
        super.startOpen($$0);
    }

    @Override
    public void stopOpen(ContainerUser $$0) {
        if (this.activeChest != null) {
            this.activeChest.stopOpen($$0);
        }
        super.stopOpen($$0);
        this.activeChest = null;
    }
}

