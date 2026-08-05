/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.Validate;

public class BannerItem
extends StandingAndWallBlockItem {
    public BannerItem(Block $$0, Block $$1, Item.Properties $$2) {
        super($$0, $$1, Direction.DOWN, $$2);
        Validate.isInstanceOf(AbstractBannerBlock.class, (Object)$$0);
        Validate.isInstanceOf(AbstractBannerBlock.class, (Object)$$1);
    }

    public DyeColor getColor() {
        return ((AbstractBannerBlock)this.getBlock()).getColor();
    }
}

