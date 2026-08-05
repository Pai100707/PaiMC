/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile.throwableitemprojectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class Snowball
extends ThrowableItemProjectile {
    public Snowball(EntityType<? extends Snowball> $$0, Level $$1) {
        super((EntityType<? extends ThrowableItemProjectile>)$$0, $$1);
    }

    public Snowball(Level $$0, LivingEntity $$1, ItemStack $$2) {
        super(EntityType.SNOWBALL, $$1, $$0, $$2);
    }

    public Snowball(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
        super(EntityType.SNOWBALL, $$1, $$2, $$3, $$0, $$4);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    private ParticleOptions getParticle() {
        ItemStack $$0 = this.getItem();
        return $$0.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, $$0);
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 3) {
            ParticleOptions $$1 = this.getParticle();
            for (int $$2 = 0; $$2 < 8; ++$$2) {
                this.level().addParticle($$1, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult $$0) {
        super.onHitEntity($$0);
        Entity $$1 = $$0.getEntity();
        int $$2 = $$1 instanceof Blaze ? 3 : 0;
        $$1.hurt(this.damageSources().thrown(this, this.getOwner()), $$2);
    }

    @Override
    protected void onHit(HitResult $$0) {
        super.onHit($$0);
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}

