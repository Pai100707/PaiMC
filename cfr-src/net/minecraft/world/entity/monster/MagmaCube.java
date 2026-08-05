/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public class MagmaCube
extends Slime {
    public MagmaCube(EntityType<? extends MagmaCube> $$0, Level $$1) {
        super((EntityType<? extends Slime>)$$0, $$1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.2f);
    }

    public static boolean checkMagmaCubeSpawnRules(EntityType<MagmaCube> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getDifficulty() != Difficulty.PEACEFUL;
    }

    @Override
    public void setSize(int $$0, boolean $$1) {
        super.setSize($$0, $$1);
        this.getAttribute(Attributes.ARMOR).setBaseValue($$0 * 3);
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    protected ParticleOptions getParticleType() {
        return ParticleTypes.FLAME;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected int getJumpDelay() {
        return super.getJumpDelay() * 4;
    }

    @Override
    protected void decreaseSquish() {
        this.targetSquish *= 0.9f;
    }

    @Override
    public void jumpFromGround() {
        Vec3 $$0 = this.getDeltaMovement();
        float $$1 = (float)this.getSize() * 0.1f;
        this.setDeltaMovement($$0.x, this.getJumpPower() + $$1, $$0.z);
        this.needsSync = true;
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> $$0) {
        if ($$0 == FluidTags.LAVA) {
            Vec3 $$1 = this.getDeltaMovement();
            this.setDeltaMovement($$1.x, 0.22f + (float)this.getSize() * 0.05f, $$1.z);
            this.needsSync = true;
        } else {
            super.jumpInLiquid($$0);
        }
    }

    @Override
    protected boolean isDealsDamage() {
        return this.isEffectiveAi();
    }

    @Override
    protected float getAttackDamage() {
        return super.getAttackDamage() + 2.0f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        if (this.isTiny()) {
            return SoundEvents.MAGMA_CUBE_HURT_SMALL;
        }
        return SoundEvents.MAGMA_CUBE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isTiny()) {
            return SoundEvents.MAGMA_CUBE_DEATH_SMALL;
        }
        return SoundEvents.MAGMA_CUBE_DEATH;
    }

    @Override
    protected SoundEvent getSquishSound() {
        if (this.isTiny()) {
            return SoundEvents.MAGMA_CUBE_SQUISH_SMALL;
        }
        return SoundEvents.MAGMA_CUBE_SQUISH;
    }

    @Override
    protected SoundEvent getJumpSound() {
        return SoundEvents.MAGMA_CUBE_JUMP;
    }
}

