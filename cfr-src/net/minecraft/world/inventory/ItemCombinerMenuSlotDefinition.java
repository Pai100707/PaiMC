/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerMenuSlotDefinition {
    private final List<SlotDefinition> slots;
    private final SlotDefinition resultSlot;

    ItemCombinerMenuSlotDefinition(List<SlotDefinition> $$0, SlotDefinition $$1) {
        if ($$0.isEmpty() || $$1.equals(SlotDefinition.EMPTY)) {
            throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
        }
        this.slots = $$0;
        this.resultSlot = $$1;
    }

    public static Builder create() {
        return new Builder();
    }

    public SlotDefinition getSlot(int $$0) {
        return this.slots.get($$0);
    }

    public SlotDefinition getResultSlot() {
        return this.resultSlot;
    }

    public List<SlotDefinition> getSlots() {
        return this.slots;
    }

    public int getNumOfInputSlots() {
        return this.slots.size();
    }

    public int getResultSlotIndex() {
        return this.getNumOfInputSlots();
    }

    public static final class SlotDefinition
    extends Record {
        final int slotIndex;
        private final int x;
        private final int y;
        private final Predicate<ItemStack> mayPlace;
        static final SlotDefinition EMPTY = new SlotDefinition(0, 0, 0, $$0 -> true);

        public SlotDefinition(int $$0, int $$1, int $$2, Predicate<ItemStack> $$3) {
            this.slotIndex = $$0;
            this.x = $$1;
            this.y = $$2;
            this.mayPlace = $$3;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SlotDefinition.class, "slotIndex;x;y;mayPlace", "slotIndex", "x", "y", "mayPlace"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SlotDefinition.class, "slotIndex;x;y;mayPlace", "slotIndex", "x", "y", "mayPlace"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SlotDefinition.class, "slotIndex;x;y;mayPlace", "slotIndex", "x", "y", "mayPlace"}, this, $$0);
        }

        public int slotIndex() {
            return this.slotIndex;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public Predicate<ItemStack> mayPlace() {
            return this.mayPlace;
        }
    }

    public static class Builder {
        private final List<SlotDefinition> inputSlots = new ArrayList<SlotDefinition>();
        private SlotDefinition resultSlot = SlotDefinition.EMPTY;

        public Builder withSlot(int $$0, int $$1, int $$2, Predicate<ItemStack> $$3) {
            this.inputSlots.add(new SlotDefinition($$0, $$1, $$2, $$3));
            return this;
        }

        public Builder withResultSlot(int $$02, int $$1, int $$2) {
            this.resultSlot = new SlotDefinition($$02, $$1, $$2, $$0 -> false);
            return this;
        }

        public ItemCombinerMenuSlotDefinition build() {
            int $$0 = this.inputSlots.size();
            for (int $$1 = 0; $$1 < $$0; ++$$1) {
                SlotDefinition $$2 = this.inputSlots.get($$1);
                if ($$2.slotIndex == $$1) continue;
                throw new IllegalArgumentException("Expected input slots to have continous indexes");
            }
            if (this.resultSlot.slotIndex != $$0) {
                throw new IllegalArgumentException("Expected result slot index to follow last input slot");
            }
            return new ItemCombinerMenuSlotDefinition(this.inputSlots, this.resultSlot);
        }
    }
}

