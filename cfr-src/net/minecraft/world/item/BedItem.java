/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BedItem
extends BlockItem {
    public BedItem(Block $$0, Item.Properties $$1) {
        super($$0, $$1);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext $$0, BlockState $$1) {
        return $$0.getLevel().setBlock($$0.getClickedPos(), $$1, 26);
    }
}

