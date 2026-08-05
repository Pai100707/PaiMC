/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class GlowItemFrame
extends ItemFrame {
    public GlowItemFrame(EntityType<? extends ItemFrame> $$0, Level $$1) {
        super($$0, $$1);
    }

    public GlowItemFrame(Level $$0, BlockPos $$1, Direction $$2) {
        super(EntityType.GLOW_ITEM_FRAME, $$0, $$1, $$2);
    }

    @Override
    public SoundEvent getRemoveItemSound() {
        return SoundEvents.GLOW_ITEM_FRAME_REMOVE_ITEM;
    }

    @Override
    public SoundEvent getBreakSound() {
        return SoundEvents.GLOW_ITEM_FRAME_BREAK;
    }

    @Override
    public SoundEvent getPlaceSound() {
        return SoundEvents.GLOW_ITEM_FRAME_PLACE;
    }

    @Override
    public SoundEvent getAddItemSound() {
        return SoundEvents.GLOW_ITEM_FRAME_ADD_ITEM;
    }

    @Override
    public SoundEvent getRotateItemSound() {
        return SoundEvents.GLOW_ITEM_FRAME_ROTATE_ITEM;
    }

    @Override
    protected ItemStack getFrameItemStack() {
        return new ItemStack(Items.GLOW_ITEM_FRAME);
    }
}

