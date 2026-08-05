/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;

public record SlotsPredicate(Map<SlotRange, ItemPredicate> slots) {
    public static final Codec<SlotsPredicate> CODEC = Codec.unboundedMap(SlotRanges.CODEC, ItemPredicate.CODEC).xmap(SlotsPredicate::new, SlotsPredicate::slots);

    public boolean matches(SlotProvider $$0) {
        for (Map.Entry<SlotRange, ItemPredicate> $$1 : this.slots.entrySet()) {
            if (SlotsPredicate.matchSlots($$0, $$1.getValue(), $$1.getKey().slots())) continue;
            return false;
        }
        return true;
    }

    private static boolean matchSlots(SlotProvider $$0, ItemPredicate $$1, IntList $$2) {
        for (int $$3 = 0; $$3 < $$2.size(); ++$$3) {
            int $$4 = $$2.getInt($$3);
            SlotAccess $$5 = $$0.getSlot($$4);
            if ($$5 == null || !$$1.test($$5.get())) continue;
            return true;
        }
        return false;
    }
}

