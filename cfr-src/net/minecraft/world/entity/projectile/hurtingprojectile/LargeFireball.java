/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile.hurtingprojectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LargeFireball
extends Fireball {
    private static final byte DEFAULT_EXPLOSION_POWER = 1;
    private int explosionPower = 1;

    public LargeFireball(EntityType<? extends LargeFireball> $$0, Level $$1) {
        super((EntityType<? extends Fireball>)$$0, $$1);
    }

    public LargeFireball(Level $$0, LivingEntity $$1, Vec3 $$2, int $$3) {
        super((EntityType<? extends Fireball>)EntityType.FIREBALL, $$1, $$2, $$0);
        this.explosionPower = $$3;
    }

    @Override
    protected void onHit(HitResult $$0) {
        super.onHit($$0);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            boolean $$2 = $$1.getGameRules().get(GameRules.MOB_GRIEFING);
            this.level().explode((Entity)this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, $$2, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }

    /*
     * WARNING - void declaration
     */
    @Override
    protected void onHitEntity(EntityHitResult $$0) {
        void $$2;
        super.onHitEntity($$0);
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel $$1 = (ServerLevel)level;
        Entity $$3 = $$0.getEntity();
        Entity $$4 = this.getOwner();
        DamageSource $$5 = this.damageSources().fireball(this, $$4);
        $$3.hurtServer((ServerLevel)$$2, $$5, 6.0f);
        EnchantmentHelper.doPostAttackEffects((ServerLevel)$$2, $$3, $$5);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.explosionPower = $$0.getByteOr("ExplosionPower", (byte)1);
    }
}

