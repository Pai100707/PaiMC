/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public interface CrossbowAttackMob
extends RangedAttackMob {
    public void setChargingCrossbow(boolean var1);

    public @Nullable LivingEntity getTarget();

    public void onCrossbowAttackPerformed();

    default public void performCrossbowAttack(LivingEntity $$0, float $$1) {
        InteractionHand $$2 = ProjectileUtil.getWeaponHoldingHand($$0, Items.CROSSBOW);
        ItemStack $$3 = $$0.getItemInHand($$2);
        Item item = $$3.getItem();
        if (item instanceof CrossbowItem) {
            CrossbowItem $$4 = (CrossbowItem)item;
            $$4.performShooting($$0.level(), $$0, $$2, $$3, $$1, 14 - $$0.level().getDifficulty().getId() * 4, this.getTarget());
        }
        this.onCrossbowAttackPerformed();
    }
}

