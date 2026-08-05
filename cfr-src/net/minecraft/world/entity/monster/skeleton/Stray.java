/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.skeleton;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public class Stray
extends AbstractSkeleton {
    public Stray(EntityType<? extends Stray> $$0, Level $$1) {
        super((EntityType<? extends AbstractSkeleton>)$$0, $$1);
    }

    public static boolean checkStraySpawnRules(EntityType<Stray> $$0, ServerLevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        BlockPos $$5 = $$3;
        while ($$1.getBlockState($$5 = $$5.above()).is(Blocks.POWDER_SNOW)) {
        }
        return Monster.checkMonsterSpawnRules($$0, $$1, $$2, $$3, $$4) && (EntitySpawnReason.isSpawner($$2) || $$1.canSeeSky($$5.below()));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.STRAY_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.STRAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRAY_DEATH;
    }

    @Override
    SoundEvent getStepSound() {
        return SoundEvents.STRAY_STEP;
    }

    @Override
    protected AbstractArrow getArrow(ItemStack $$0, float $$1, @Nullable ItemStack $$2) {
        AbstractArrow $$3 = super.getArrow($$0, $$1, $$2);
        if ($$3 instanceof Arrow) {
            ((Arrow)$$3).addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600));
        }
        return $$3;
    }
}

