/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.player;

import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerEquipment
extends EntityEquipment {
    private final Player player;

    public PlayerEquipment(Player $$0) {
        this.player = $$0;
    }

    @Override
    public ItemStack set(EquipmentSlot $$0, ItemStack $$1) {
        if ($$0 == EquipmentSlot.MAINHAND) {
            return this.player.getInventory().setSelectedItem($$1);
        }
        return super.set($$0, $$1);
    }

    @Override
    public ItemStack get(EquipmentSlot $$0) {
        if ($$0 == EquipmentSlot.MAINHAND) {
            return this.player.getInventory().getSelectedItem();
        }
        return super.get($$0);
    }

    @Override
    public boolean isEmpty() {
        return this.player.getInventory().getSelectedItem().isEmpty() && super.isEmpty();
    }
}

