/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface SlotAccess {
    public ItemStack get();

    public boolean set(ItemStack var1);

    public static SlotAccess of(final Supplier<ItemStack> $$0, final Consumer<ItemStack> $$1) {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return (ItemStack)$$0.get();
            }

            @Override
            public boolean set(ItemStack $$02) {
                $$1.accept($$02);
                return true;
            }
        };
    }

    public static SlotAccess forEquipmentSlot(final LivingEntity $$0, final EquipmentSlot $$1, final Predicate<ItemStack> $$2) {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return $$0.getItemBySlot($$1);
            }

            @Override
            public boolean set(ItemStack $$02) {
                if (!$$2.test($$02)) {
                    return false;
                }
                $$0.setItemSlot($$1, $$02);
                return true;
            }
        };
    }

    public static SlotAccess forEquipmentSlot(LivingEntity $$02, EquipmentSlot $$1) {
        return SlotAccess.forEquipmentSlot($$02, $$1, $$0 -> true);
    }

    public static SlotAccess forListElement(final List<ItemStack> $$0, final int $$1) {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return (ItemStack)$$0.get($$1);
            }

            @Override
            public boolean set(ItemStack $$02) {
                $$0.set($$1, $$02);
                return true;
            }
        };
    }
}

