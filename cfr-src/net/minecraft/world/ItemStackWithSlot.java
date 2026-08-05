/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public record ItemStackWithSlot(int slot, ItemStack stack) {
    public static final Codec<ItemStackWithSlot> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)ExtraCodecs.UNSIGNED_BYTE.fieldOf("Slot").orElse((Object)0).forGetter(ItemStackWithSlot::slot), (App)ItemStack.MAP_CODEC.forGetter(ItemStackWithSlot::stack)).apply((Applicative)$$0, ItemStackWithSlot::new));

    public boolean isValidInContainer(int $$0) {
        return this.slot >= 0 && this.slot < $$0;
    }
}

