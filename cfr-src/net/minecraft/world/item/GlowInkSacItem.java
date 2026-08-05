/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class GlowInkSacItem
extends Item
implements SignApplicator {
    public GlowInkSacItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public boolean tryApplyToSign(Level $$02, SignBlockEntity $$1, boolean $$2, Player $$3) {
        if ($$1.updateText($$0 -> $$0.setHasGlowingText(true), $$2)) {
            $$02.playSound(null, $$1.getBlockPos(), SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}

