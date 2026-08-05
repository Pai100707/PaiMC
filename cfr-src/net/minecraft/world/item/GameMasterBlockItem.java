/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class GameMasterBlockItem
extends BlockItem {
    public GameMasterBlockItem(Block $$0, Item.Properties $$1) {
        super($$0, $$1);
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext $$0) {
        Player $$1 = $$0.getPlayer();
        return $$1 == null || $$1.canUseGameMasterBlocks() ? super.getPlacementState($$0) : null;
    }
}

