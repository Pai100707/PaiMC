/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile.hurtingprojectile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SmallFireball
extends Fireball {
    public SmallFireball(EntityType<? extends SmallFireball> $$0, Level $$1) {
        super((EntityType<? extends Fireball>)$$0, $$1);
    }

    public SmallFireball(Level $$0, LivingEntity $$1, Vec3 $$2) {
        super((EntityType<? extends Fireball>)EntityType.SMALL_FIREBALL, $$1, $$2, $$0);
    }

    public SmallFireball(Level $$0, double $$1, double $$2, double $$3, Vec3 $$4) {
        super((EntityType<? extends Fireball>)EntityType.SMALL_FIREBALL, $$1, $$2, $$3, $$4, $$0);
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
        int $$5 = $$3.getRemainingFireTicks();
        $$3.igniteForSeconds(5.0f);
        DamageSource $$6 = this.damageSources().fireball(this, $$4);
        if (!$$3.hurtServer((ServerLevel)$$2, $$6, 5.0f)) {
            $$3.setRemainingFireTicks($$5);
        } else {
            EnchantmentHelper.doPostAttackEffects((ServerLevel)$$2, $$3, $$6);
        }
    }

    /*
     * WARNING - void declaration
     */
    @Override
    protected void onHitBlock(BlockHitResult $$0) {
        void $$2;
        super.onHitBlock($$0);
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel $$1 = (ServerLevel)level;
        Entity $$3 = this.getOwner();
        if (!($$3 instanceof Mob) || $$2.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
            BlockPos $$4 = $$0.getBlockPos().relative($$0.getDirection());
            if (this.level().isEmptyBlock($$4)) {
                this.level().setBlockAndUpdate($$4, BaseFireBlock.getState(this.level(), $$4));
            }
        }
    }

    @Override
    protected void onHit(HitResult $$0) {
        super.onHit($$0);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }
}

