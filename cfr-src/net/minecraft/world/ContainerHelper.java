/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ContainerHelper {
    public static final String TAG_ITEMS = "Items";

    public static ItemStack removeItem(List<ItemStack> $$0, int $$1, int $$2) {
        if ($$1 < 0 || $$1 >= $$0.size() || $$0.get($$1).isEmpty() || $$2 <= 0) {
            return ItemStack.EMPTY;
        }
        return $$0.get($$1).split($$2);
    }

    public static ItemStack takeItem(List<ItemStack> $$0, int $$1) {
        if ($$1 < 0 || $$1 >= $$0.size()) {
            return ItemStack.EMPTY;
        }
        return $$0.set($$1, ItemStack.EMPTY);
    }

    public static void saveAllItems(ValueOutput $$0, NonNullList<ItemStack> $$1) {
        ContainerHelper.saveAllItems($$0, $$1, true);
    }

    public static void saveAllItems(ValueOutput $$0, NonNullList<ItemStack> $$1, boolean $$2) {
        ValueOutput.TypedOutputList<ItemStackWithSlot> $$3 = $$0.list(TAG_ITEMS, ItemStackWithSlot.CODEC);
        for (int $$4 = 0; $$4 < $$1.size(); ++$$4) {
            ItemStack $$5 = $$1.get($$4);
            if ($$5.isEmpty()) continue;
            $$3.add(new ItemStackWithSlot($$4, $$5));
        }
        if ($$3.isEmpty() && !$$2) {
            $$0.discard(TAG_ITEMS);
        }
    }

    public static void loadAllItems(ValueInput $$0, NonNullList<ItemStack> $$1) {
        for (ItemStackWithSlot $$2 : $$0.listOrEmpty(TAG_ITEMS, ItemStackWithSlot.CODEC)) {
            if (!$$2.isValidInContainer($$1.size())) continue;
            $$1.set($$2.slot(), $$2.stack());
        }
    }

    public static int clearOrCountMatchingItems(Container $$0, Predicate<ItemStack> $$1, int $$2, boolean $$3) {
        int $$4 = 0;
        for (int $$5 = 0; $$5 < $$0.getContainerSize(); ++$$5) {
            ItemStack $$6 = $$0.getItem($$5);
            int $$7 = ContainerHelper.clearOrCountMatchingItems($$6, $$1, $$2 - $$4, $$3);
            if ($$7 > 0 && !$$3 && $$6.isEmpty()) {
                $$0.setItem($$5, ItemStack.EMPTY);
            }
            $$4 += $$7;
        }
        return $$4;
    }

    public static int clearOrCountMatchingItems(ItemStack $$0, Predicate<ItemStack> $$1, int $$2, boolean $$3) {
        if ($$0.isEmpty() || !$$1.test($$0)) {
            return 0;
        }
        if ($$3) {
            return $$0.getCount();
        }
        int $$4 = $$2 < 0 ? $$0.getCount() : Math.min($$2, $$0.getCount());
        $$0.shrink($$4);
        return $$4;
    }
}

