/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntLists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.SlotRange;
import org.jspecify.annotations.Nullable;

public class SlotRanges {
    private static final List<SlotRange> SLOTS = Util.make(new ArrayList(), $$0 -> {
        SlotRanges.addSingleSlot($$0, "contents", 0);
        SlotRanges.addSlotRange($$0, "container.", 0, 54);
        SlotRanges.addSlotRange($$0, "hotbar.", 0, 9);
        SlotRanges.addSlotRange($$0, "inventory.", 9, 27);
        SlotRanges.addSlotRange($$0, "enderchest.", 200, 27);
        SlotRanges.addSlotRange($$0, "villager.", 300, 8);
        SlotRanges.addSlotRange($$0, "horse.", 500, 15);
        int $$1 = EquipmentSlot.MAINHAND.getIndex(98);
        int $$2 = EquipmentSlot.OFFHAND.getIndex(98);
        SlotRanges.addSingleSlot($$0, "weapon", $$1);
        SlotRanges.addSingleSlot($$0, "weapon.mainhand", $$1);
        SlotRanges.addSingleSlot($$0, "weapon.offhand", $$2);
        SlotRanges.addSlots($$0, "weapon.*", $$1, $$2);
        int $$3 = EquipmentSlot.HEAD.getIndex(100);
        int $$4 = EquipmentSlot.CHEST.getIndex(100);
        int $$5 = EquipmentSlot.LEGS.getIndex(100);
        int $$6 = EquipmentSlot.FEET.getIndex(100);
        int $$7 = EquipmentSlot.BODY.getIndex(105);
        SlotRanges.addSingleSlot($$0, "armor.head", $$3);
        SlotRanges.addSingleSlot($$0, "armor.chest", $$4);
        SlotRanges.addSingleSlot($$0, "armor.legs", $$5);
        SlotRanges.addSingleSlot($$0, "armor.feet", $$6);
        SlotRanges.addSingleSlot($$0, "armor.body", $$7);
        SlotRanges.addSlots($$0, "armor.*", $$3, $$4, $$5, $$6, $$7);
        SlotRanges.addSingleSlot($$0, "saddle", EquipmentSlot.SADDLE.getIndex(106));
        SlotRanges.addSingleSlot($$0, "horse.chest", 499);
        SlotRanges.addSingleSlot($$0, "player.cursor", 499);
        SlotRanges.addSlotRange($$0, "player.crafting.", 500, 4);
    });
    public static final Codec<SlotRange> CODEC = StringRepresentable.fromValues(() -> (SlotRange[])SLOTS.toArray(SlotRange[]::new));
    private static final Function<String, @Nullable SlotRange> NAME_LOOKUP = StringRepresentable.createNameLookup((StringRepresentable[])((SlotRange[])SLOTS.toArray(SlotRange[]::new)));

    private static SlotRange create(String $$0, int $$1) {
        return SlotRange.of($$0, IntLists.singleton((int)$$1));
    }

    private static SlotRange create(String $$0, IntList $$1) {
        return SlotRange.of($$0, IntLists.unmodifiable((IntList)$$1));
    }

    private static SlotRange create(String $$0, int ... $$1) {
        return SlotRange.of($$0, IntList.of((int[])$$1));
    }

    private static void addSingleSlot(List<SlotRange> $$0, String $$1, int $$2) {
        $$0.add(SlotRanges.create($$1, $$2));
    }

    private static void addSlotRange(List<SlotRange> $$0, String $$1, int $$2, int $$3) {
        IntArrayList $$4 = new IntArrayList($$3);
        for (int $$5 = 0; $$5 < $$3; ++$$5) {
            int $$6 = $$2 + $$5;
            $$0.add(SlotRanges.create($$1 + $$5, $$6));
            $$4.add($$6);
        }
        $$0.add(SlotRanges.create($$1 + "*", (IntList)$$4));
    }

    private static void addSlots(List<SlotRange> $$0, String $$1, int ... $$2) {
        $$0.add(SlotRanges.create($$1, $$2));
    }

    public static @Nullable SlotRange nameToIds(String $$0) {
        return NAME_LOOKUP.apply($$0);
    }

    public static Stream<String> allNames() {
        return SLOTS.stream().map(StringRepresentable::getSerializedName);
    }

    public static Stream<String> singleSlotNames() {
        return SLOTS.stream().filter($$0 -> $$0.size() == 1).map(StringRepresentable::getSerializedName);
    }
}

