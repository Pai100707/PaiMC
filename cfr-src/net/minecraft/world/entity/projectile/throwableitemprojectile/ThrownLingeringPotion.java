/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile.throwableitemprojectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownLingeringPotion
extends AbstractThrownPotion {
    public ThrownLingeringPotion(EntityType<? extends ThrownLingeringPotion> $$0, Level $$1) {
        super((EntityType<? extends AbstractThrownPotion>)$$0, $$1);
    }

    public ThrownLingeringPotion(Level $$0, LivingEntity $$1, ItemStack $$2) {
        super(EntityType.LINGERING_POTION, $$0, $$1, $$2);
    }

    public ThrownLingeringPotion(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
        super(EntityType.LINGERING_POTION, $$0, $$1, $$2, $$3, $$4);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.LINGERING_POTION;
    }

    @Override
    public void onHitAsPotion(ServerLevel $$0, ItemStack $$1, HitResult $$2) {
        AreaEffectCloud $$3 = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            LivingEntity $$4 = (LivingEntity)entity;
            $$3.setOwner($$4);
        }
        $$3.setRadius(3.0f);
        $$3.setRadiusOnUse(-0.5f);
        $$3.setDuration(600);
        $$3.setWaitTime(10);
        $$3.setRadiusPerTick(-$$3.getRadius() / (float)$$3.getDuration());
        $$3.applyComponentsFromItemStack($$1);
        $$0.addFreshEntity($$3);
    }
}

