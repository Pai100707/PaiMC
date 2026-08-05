/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public record LockCode(ItemPredicate predicate) {
    public static final LockCode NO_LOCK = new LockCode(ItemPredicate.Builder.item().build());
    public static final Codec<LockCode> CODEC = ItemPredicate.CODEC.xmap(LockCode::new, LockCode::predicate);
    public static final String TAG_LOCK = "lock";

    public boolean unlocksWith(ItemStack $$0) {
        return this.predicate.test($$0);
    }

    public void addToTag(ValueOutput $$0) {
        if (this != NO_LOCK) {
            $$0.store(TAG_LOCK, CODEC, this);
        }
    }

    public boolean canUnlock(Player $$0) {
        return $$0.isSpectator() || this.unlocksWith($$0.getMainHandItem());
    }

    public static LockCode fromTag(ValueInput $$0) {
        return $$0.read(TAG_LOCK, CODEC).orElse(NO_LOCK);
    }
}

