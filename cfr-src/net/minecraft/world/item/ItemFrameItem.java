/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemFrameItem
extends HangingEntityItem {
    public ItemFrameItem(EntityType<? extends HangingEntity> $$0, Item.Properties $$1) {
        super($$0, $$1);
    }

    @Override
    protected boolean mayPlace(Player $$0, Direction $$1, ItemStack $$2, BlockPos $$3) {
        return !$$0.level().isOutsideBuildHeight($$3) && $$0.mayUseItemAt($$3, $$1, $$2);
    }
}

