/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile.throwableitemprojectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownExperienceBottle
extends ThrowableItemProjectile {
    public ThrownExperienceBottle(EntityType<? extends ThrownExperienceBottle> $$0, Level $$1) {
        super((EntityType<? extends ThrowableItemProjectile>)$$0, $$1);
    }

    public ThrownExperienceBottle(Level $$0, LivingEntity $$1, ItemStack $$2) {
        super(EntityType.EXPERIENCE_BOTTLE, $$1, $$0, $$2);
    }

    public ThrownExperienceBottle(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
        super(EntityType.EXPERIENCE_BOTTLE, $$1, $$2, $$3, $$0, $$4);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.07;
    }

    @Override
    protected void onHit(HitResult $$0) {
        super.onHit($$0);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            $$1.levelEvent(2002, this.blockPosition(), -13083194);
            int $$2 = 3 + $$1.random.nextInt(5) + $$1.random.nextInt(5);
            if ($$0 instanceof BlockHitResult) {
                BlockHitResult $$3 = (BlockHitResult)$$0;
                Vec3 $$4 = $$3.getDirection().getUnitVec3();
                ExperienceOrb.awardWithDirection($$1, $$0.getLocation(), $$4, $$2);
            } else {
                ExperienceOrb.awardWithDirection($$1, $$0.getLocation(), this.getDeltaMovement().scale(-1.0), $$2);
            }
            this.discard();
        }
    }
}

