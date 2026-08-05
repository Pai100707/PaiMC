/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public interface EquipmentUser {
    public void setItemSlot(EquipmentSlot var1, ItemStack var2);

    public ItemStack getItemBySlot(EquipmentSlot var1);

    public void setDropChance(EquipmentSlot var1, float var2);

    default public void equip(EquipmentTable $$0, LootParams $$1) {
        this.equip($$0.lootTable(), $$1, $$0.slotDropChances());
    }

    default public void equip(ResourceKey<LootTable> $$0, LootParams $$1, Map<EquipmentSlot, Float> $$2) {
        this.equip($$0, $$1, 0L, $$2);
    }

    default public void equip(ResourceKey<LootTable> $$0, LootParams $$1, long $$2, Map<EquipmentSlot, Float> $$3) {
        LootTable $$4 = $$1.getLevel().getServer().reloadableRegistries().getLootTable($$0);
        if ($$4 == LootTable.EMPTY) {
            return;
        }
        ObjectArrayList<ItemStack> $$5 = $$4.getRandomItems($$1, $$2);
        ArrayList<EquipmentSlot> $$6 = new ArrayList<EquipmentSlot>();
        for (ItemStack $$7 : $$5) {
            EquipmentSlot $$8 = this.resolveSlot($$7, $$6);
            if ($$8 == null) continue;
            ItemStack $$9 = $$8.limit($$7);
            this.setItemSlot($$8, $$9);
            Float $$10 = $$3.get($$8);
            if ($$10 != null) {
                this.setDropChance($$8, $$10.floatValue());
            }
            $$6.add($$8);
        }
    }

    default public @Nullable EquipmentSlot resolveSlot(ItemStack $$0, List<EquipmentSlot> $$1) {
        if ($$0.isEmpty()) {
            return null;
        }
        Equippable $$2 = $$0.get(DataComponents.EQUIPPABLE);
        if ($$2 != null) {
            EquipmentSlot $$3 = $$2.slot();
            if (!$$1.contains($$3)) {
                return $$3;
            }
        } else if (!$$1.contains(EquipmentSlot.MAINHAND)) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }
}

