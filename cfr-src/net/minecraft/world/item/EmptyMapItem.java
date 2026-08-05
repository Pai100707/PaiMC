/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;

public class EmptyMapItem
extends Item {
    public EmptyMapItem(Item.Properties $$0) {
        super($$0);
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
        void $$5;
        ItemStack $$3 = $$1.getItemInHand($$2);
        if (!($$0 instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel $$4 = (ServerLevel)$$0;
        $$3.consume(1, $$1);
        $$1.awardStat(Stats.ITEM_USED.get(this));
        $$5.playSound(null, $$1, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, $$1.getSoundSource(), 1.0f, 1.0f);
        ItemStack $$6 = MapItem.create((ServerLevel)$$5, $$1.getBlockX(), $$1.getBlockZ(), (byte)0, true, false);
        if ($$3.isEmpty()) {
            return InteractionResult.SUCCESS.heldItemTransformedTo($$6);
        }
        if (!$$1.getInventory().add($$6.copy())) {
            $$1.drop($$6, false);
        }
        return InteractionResult.SUCCESS;
    }
}

