/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class DolphinJumpGoal
extends JumpGoal {
    private static final int[] STEPS_TO_CHECK = new int[]{0, 1, 4, 5, 6, 7};
    private final Dolphin dolphin;
    private final int interval;
    private boolean breached;

    public DolphinJumpGoal(Dolphin $$0, int $$1) {
        this.dolphin = $$0;
        this.interval = DolphinJumpGoal.reducedTickDelay($$1);
    }

    @Override
    public boolean canUse() {
        if (this.dolphin.getRandom().nextInt(this.interval) != 0) {
            return false;
        }
        Direction $$0 = this.dolphin.getMotionDirection();
        int $$1 = $$0.getStepX();
        int $$2 = $$0.getStepZ();
        BlockPos $$3 = this.dolphin.blockPosition();
        for (int $$4 : STEPS_TO_CHECK) {
            if (this.waterIsClear($$3, $$1, $$2, $$4) && this.surfaceIsClear($$3, $$1, $$2, $$4)) continue;
            return false;
        }
        return true;
    }

    private boolean waterIsClear(BlockPos $$0, int $$1, int $$2, int $$3) {
        BlockPos $$4 = $$0.offset($$1 * $$3, 0, $$2 * $$3);
        return this.dolphin.level().getFluidState($$4).is(FluidTags.WATER) && !this.dolphin.level().getBlockState($$4).blocksMotion();
    }

    private boolean surfaceIsClear(BlockPos $$0, int $$1, int $$2, int $$3) {
        return this.dolphin.level().getBlockState($$0.offset($$1 * $$3, 1, $$2 * $$3)).isAir() && this.dolphin.level().getBlockState($$0.offset($$1 * $$3, 2, $$2 * $$3)).isAir();
    }

    @Override
    public boolean canContinueToUse() {
        double $$0 = this.dolphin.getDeltaMovement().y;
        return !($$0 * $$0 < (double)0.03f && this.dolphin.getXRot() != 0.0f && Math.abs(this.dolphin.getXRot()) < 10.0f && this.dolphin.isInWater() || this.dolphin.onGround());
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        Direction $$0 = this.dolphin.getMotionDirection();
        this.dolphin.setDeltaMovement(this.dolphin.getDeltaMovement().add((double)$$0.getStepX() * 0.6, 0.7, (double)$$0.getStepZ() * 0.6));
        this.dolphin.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.dolphin.setXRot(0.0f);
    }

    @Override
    public void tick() {
        boolean $$0 = this.breached;
        if (!$$0) {
            FluidState $$1 = this.dolphin.level().getFluidState(this.dolphin.blockPosition());
            this.breached = $$1.is(FluidTags.WATER);
        }
        if (this.breached && !$$0) {
            this.dolphin.playSound(SoundEvents.DOLPHIN_JUMP, 1.0f, 1.0f);
        }
        Vec3 $$2 = this.dolphin.getDeltaMovement();
        if ($$2.y * $$2.y < (double)0.03f && this.dolphin.getXRot() != 0.0f) {
            this.dolphin.setXRot(Mth.rotLerp(0.2f, this.dolphin.getXRot(), 0.0f));
        } else if ($$2.length() > (double)1.0E-5f) {
            double $$3 = $$2.horizontalDistance();
            double $$4 = Math.atan2(-$$2.y, $$3) * 57.2957763671875;
            this.dolphin.setXRot((float)$$4);
        }
    }
}

