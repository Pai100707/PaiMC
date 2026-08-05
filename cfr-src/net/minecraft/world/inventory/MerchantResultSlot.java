/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;

public class MerchantResultSlot
extends Slot {
    private final MerchantContainer slots;
    private final Player player;
    private int removeCount;
    private final Merchant merchant;

    public MerchantResultSlot(Player $$0, Merchant $$1, MerchantContainer $$2, int $$3, int $$4, int $$5) {
        super($$2, $$3, $$4, $$5);
        this.player = $$0;
        this.merchant = $$1;
        this.slots = $$2;
    }

    @Override
    public boolean mayPlace(ItemStack $$0) {
        return false;
    }

    @Override
    public ItemStack remove(int $$0) {
        if (this.hasItem()) {
            this.removeCount += Math.min($$0, this.getItem().getCount());
        }
        return super.remove($$0);
    }

    @Override
    protected void onQuickCraft(ItemStack $$0, int $$1) {
        this.removeCount += $$1;
        this.checkTakeAchievements($$0);
    }

    @Override
    protected void checkTakeAchievements(ItemStack $$0) {
        $$0.onCraftedBy(this.player, this.removeCount);
        this.removeCount = 0;
    }

    @Override
    public void onTake(Player $$0, ItemStack $$1) {
        this.checkTakeAchievements($$1);
        MerchantOffer $$2 = this.slots.getActiveOffer();
        if ($$2 != null) {
            ItemStack $$4;
            ItemStack $$3 = this.slots.getItem(0);
            if ($$2.take($$3, $$4 = this.slots.getItem(1)) || $$2.take($$4, $$3)) {
                this.merchant.notifyTrade($$2);
                $$0.awardStat(Stats.TRADED_WITH_VILLAGER);
                this.slots.setItem(0, $$3);
                this.slots.setItem(1, $$4);
            }
            this.merchant.overrideXp(this.merchant.getVillagerXp() + $$2.getXp());
        }
    }
}

