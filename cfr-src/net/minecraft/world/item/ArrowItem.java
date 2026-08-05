/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ArrowItem
extends Item
implements ProjectileItem {
    public ArrowItem(Item.Properties $$0) {
        super($$0);
    }

    public AbstractArrow createArrow(Level $$0, ItemStack $$1, LivingEntity $$2, @Nullable ItemStack $$3) {
        return new Arrow($$0, $$2, $$1.copyWithCount(1), $$3);
    }

    @Override
    public Projectile asProjectile(Level $$0, Position $$1, ItemStack $$2, Direction $$3) {
        Arrow $$4 = new Arrow($$0, $$1.x(), $$1.y(), $$1.z(), $$2.copyWithCount(1), null);
        $$4.pickup = AbstractArrow.Pickup.ALLOWED;
        return $$4;
    }
}

