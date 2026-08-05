/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ThrowableProjectile
extends Projectile {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25f;

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> $$0, Level $$1) {
        super((EntityType<? extends Projectile>)$$0, $$1);
    }

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> $$0, double $$1, double $$2, double $$3, Level $$4) {
        this($$0, $$4);
        this.setPos($$1, $$2, $$3);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double $$0) {
        if (this.tickCount < 2 && $$0 < 12.25) {
            return false;
        }
        double $$1 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN($$1)) {
            $$1 = 4.0;
        }
        return $$0 < ($$1 *= 64.0) * $$1;
    }

    @Override
    public boolean canUsePortal(boolean $$0) {
        return true;
    }

    @Override
    public void tick() {
        Vec3 $$2;
        this.handleFirstTickBubbleColumn();
        this.applyGravity();
        this.applyInertia();
        HitResult $$0 = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if ($$0.getType() != HitResult.Type.MISS) {
            Vec3 $$1 = $$0.getLocation();
        } else {
            $$2 = this.position().add(this.getDeltaMovement());
        }
        this.setPos($$2);
        this.updateRotation();
        this.applyEffectsFromBlocks();
        super.tick();
        if ($$0.getType() != HitResult.Type.MISS && this.isAlive()) {
            this.hitTargetOrDeflectSelf($$0);
        }
    }

    private void applyInertia() {
        float $$5;
        Vec3 $$0 = this.getDeltaMovement();
        Vec3 $$1 = this.position();
        if (this.isInWater()) {
            for (int $$2 = 0; $$2 < 4; ++$$2) {
                float $$3 = 0.25f;
                this.level().addParticle(ParticleTypes.BUBBLE, $$1.x - $$0.x * 0.25, $$1.y - $$0.y * 0.25, $$1.z - $$0.z * 0.25, $$0.x, $$0.y, $$0.z);
            }
            float $$4 = 0.8f;
        } else {
            $$5 = 0.99f;
        }
        this.setDeltaMovement($$0.scale($$5));
    }

    private void handleFirstTickBubbleColumn() {
        if (this.firstTick) {
            for (BlockPos $$0 : BlockPos.betweenClosed(this.getBoundingBox())) {
                BlockState $$1 = this.level().getBlockState($$0);
                if (!$$1.is(Blocks.BUBBLE_COLUMN)) continue;
                $$1.entityInside(this.level(), $$0, this, InsideBlockEffectApplier.NOOP, true);
            }
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }
}

