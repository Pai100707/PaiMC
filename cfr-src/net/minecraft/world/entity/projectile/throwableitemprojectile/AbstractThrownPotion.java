/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 */
package net.minecraft.world.entity.projectile.throwableitemprojectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class AbstractThrownPotion
extends ThrowableItemProjectile {
    public static final double SPLASH_RANGE = 4.0;
    protected static final double SPLASH_RANGE_SQ = 16.0;
    public static final Predicate<LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = $$0 -> $$0.isSensitiveToWater() || $$0.isOnFire();

    public AbstractThrownPotion(EntityType<? extends AbstractThrownPotion> $$0, Level $$1) {
        super((EntityType<? extends ThrowableItemProjectile>)$$0, $$1);
    }

    public AbstractThrownPotion(EntityType<? extends AbstractThrownPotion> $$0, Level $$1, LivingEntity $$2, ItemStack $$3) {
        super($$0, $$2, $$1, $$3);
    }

    public AbstractThrownPotion(EntityType<? extends AbstractThrownPotion> $$0, Level $$1, double $$2, double $$3, double $$4, ItemStack $$5) {
        super($$0, $$2, $$3, $$4, $$1, $$5);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHitBlock(BlockHitResult $$0) {
        super.onHitBlock($$0);
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack $$1 = this.getItem();
        Direction $$2 = $$0.getDirection();
        BlockPos $$3 = $$0.getBlockPos();
        BlockPos $$4 = $$3.relative($$2);
        PotionContents $$5 = $$1.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if ($$5.is(Potions.WATER)) {
            this.dowseFire($$4);
            this.dowseFire($$4.relative($$2.getOpposite()));
            for (Direction $$6 : Direction.Plane.HORIZONTAL) {
                this.dowseFire($$4.relative($$6));
            }
        }
    }

    /*
     * WARNING - void declaration
     */
    @Override
    protected void onHit(HitResult $$0) {
        void $$2;
        super.onHit($$0);
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel $$1 = (ServerLevel)level;
        ItemStack $$3 = this.getItem();
        PotionContents $$4 = $$3.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if ($$4.is(Potions.WATER)) {
            this.onHitAsWater((ServerLevel)$$2);
        } else if ($$4.hasEffects()) {
            this.onHitAsPotion((ServerLevel)$$2, $$3, $$0);
        }
        int $$5 = $$4.potion().isPresent() && $$4.potion().get().value().hasInstantEffects() ? 2007 : 2002;
        $$2.levelEvent($$5, this.blockPosition(), $$4.getColor());
        this.discard();
    }

    private void onHitAsWater(ServerLevel $$0) {
        AABB $$1 = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> $$2 = this.level().getEntitiesOfClass(LivingEntity.class, $$1, WATER_SENSITIVE_OR_ON_FIRE);
        for (LivingEntity $$3 : $$2) {
            double $$4 = this.distanceToSqr($$3);
            if (!($$4 < 16.0)) continue;
            if ($$3.isSensitiveToWater()) {
                $$3.hurtServer($$0, this.damageSources().indirectMagic(this, this.getOwner()), 1.0f);
            }
            if (!$$3.isOnFire() || !$$3.isAlive()) continue;
            $$3.extinguishFire();
        }
        List<Axolotl> $$5 = this.level().getEntitiesOfClass(Axolotl.class, $$1);
        for (Axolotl $$6 : $$5) {
            $$6.rehydrate();
        }
    }

    protected abstract void onHitAsPotion(ServerLevel var1, ItemStack var2, HitResult var3);

    private void dowseFire(BlockPos $$0) {
        BlockState $$1 = this.level().getBlockState($$0);
        if ($$1.is(BlockTags.FIRE)) {
            this.level().destroyBlock($$0, false, this);
        } else if (AbstractCandleBlock.isLit($$1)) {
            AbstractCandleBlock.extinguish(null, $$1, this.level(), $$0);
        } else if (CampfireBlock.isLitCampfire($$1)) {
            this.level().levelEvent(null, 1009, $$0, 0);
            CampfireBlock.dowse(this.getOwner(), this.level(), $$0, $$1);
            this.level().setBlockAndUpdate($$0, (BlockState)$$1.setValue(CampfireBlock.LIT, false));
        }
    }

    @Override
    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity $$0, DamageSource $$1) {
        double $$2 = $$0.position().x - this.position().x;
        double $$3 = $$0.position().z - this.position().z;
        return DoubleDoubleImmutablePair.of((double)$$2, (double)$$3);
    }
}

