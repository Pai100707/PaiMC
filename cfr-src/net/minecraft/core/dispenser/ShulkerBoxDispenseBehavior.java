/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;

public class ShulkerBoxDispenseBehavior
extends OptionalDispenseItemBehavior {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected ItemStack execute(BlockSource $$0, ItemStack $$1) {
        this.setSuccess(false);
        Item $$2 = $$1.getItem();
        if ($$2 instanceof BlockItem) {
            Direction $$3 = $$0.state().getValue(DispenserBlock.FACING);
            BlockPos $$4 = $$0.pos().relative($$3);
            Direction $$5 = $$0.level().isEmptyBlock($$4.below()) ? $$3 : Direction.UP;
            try {
                this.setSuccess(((BlockItem)$$2).place(new DirectionalPlaceContext((Level)$$0.level(), $$4, $$3, $$1, $$5)).consumesAction());
            }
            catch (Exception $$6) {
                LOGGER.error("Error trying to place shulker box at {}", (Object)$$4, (Object)$$6);
            }
        }
        return $$1;
    }
}

