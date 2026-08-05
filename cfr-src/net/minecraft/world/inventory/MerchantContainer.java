/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.Nullable;

public class MerchantContainer
implements Container {
    private final Merchant merchant;
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
    private @Nullable MerchantOffer activeOffer;
    private int selectionHint;
    private int futureXp;

    public MerchantContainer(Merchant $$0) {
        this.merchant = $$0;
    }

    @Override
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack $$0 : this.itemStacks) {
            if ($$0.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int $$0) {
        return this.itemStacks.get($$0);
    }

    @Override
    public ItemStack removeItem(int $$0, int $$1) {
        ItemStack $$2 = this.itemStacks.get($$0);
        if ($$0 == 2 && !$$2.isEmpty()) {
            return ContainerHelper.removeItem(this.itemStacks, $$0, $$2.getCount());
        }
        ItemStack $$3 = ContainerHelper.removeItem(this.itemStacks, $$0, $$1);
        if (!$$3.isEmpty() && this.isPaymentSlot($$0)) {
            this.updateSellItem();
        }
        return $$3;
    }

    private boolean isPaymentSlot(int $$0) {
        return $$0 == 0 || $$0 == 1;
    }

    @Override
    public ItemStack removeItemNoUpdate(int $$0) {
        return ContainerHelper.takeItem(this.itemStacks, $$0);
    }

    @Override
    public void setItem(int $$0, ItemStack $$1) {
        this.itemStacks.set($$0, $$1);
        $$1.limitSize(this.getMaxStackSize($$1));
        if (this.isPaymentSlot($$0)) {
            this.updateSellItem();
        }
    }

    @Override
    public boolean stillValid(Player $$0) {
        return this.merchant.getTradingPlayer() == $$0;
    }

    @Override
    public void setChanged() {
        this.updateSellItem();
    }

    public void updateSellItem() {
        ItemStack $$3;
        ItemStack $$2;
        this.activeOffer = null;
        if (this.itemStacks.get(0).isEmpty()) {
            ItemStack $$0 = this.itemStacks.get(1);
            ItemStack $$1 = ItemStack.EMPTY;
        } else {
            $$2 = this.itemStacks.get(0);
            $$3 = this.itemStacks.get(1);
        }
        if ($$2.isEmpty()) {
            this.setItem(2, ItemStack.EMPTY);
            this.futureXp = 0;
            return;
        }
        MerchantOffers $$4 = this.merchant.getOffers();
        if (!$$4.isEmpty()) {
            MerchantOffer $$5 = $$4.getRecipeFor($$2, $$3, this.selectionHint);
            if ($$5 == null || $$5.isOutOfStock()) {
                this.activeOffer = $$5;
                $$5 = $$4.getRecipeFor($$3, $$2, this.selectionHint);
            }
            if ($$5 != null && !$$5.isOutOfStock()) {
                this.activeOffer = $$5;
                this.setItem(2, $$5.assemble());
                this.futureXp = $$5.getXp();
            } else {
                this.setItem(2, ItemStack.EMPTY);
                this.futureXp = 0;
            }
        }
        this.merchant.notifyTradeUpdated(this.getItem(2));
    }

    public @Nullable MerchantOffer getActiveOffer() {
        return this.activeOffer;
    }

    public void setSelectionHint(int $$0) {
        this.selectionHint = $$0;
        this.updateSellItem();
    }

    @Override
    public void clearContent() {
        this.itemStacks.clear();
    }

    public int getFutureXp() {
        return this.futureXp;
    }
}

