/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class TryFindWaterGoal
extends Goal {
    private final PathfinderMob mob;

    public TryFindWaterGoal(PathfinderMob $$0) {
        this.mob = $$0;
    }

    @Override
    public boolean canUse() {
        return this.mob.onGround() && !this.mob.level().getFluidState(this.mob.blockPosition()).is(FluidTags.WATER);
    }

    @Override
    public void start() {
        Vec3i $$0 = null;
        Iterable<BlockPos> $$1 = BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 2.0), Mth.floor(this.mob.getY() - 2.0), Mth.floor(this.mob.getZ() - 2.0), Mth.floor(this.mob.getX() + 2.0), this.mob.getBlockY(), Mth.floor(this.mob.getZ() + 2.0));
        for (BlockPos $$2 : $$1) {
            if (!this.mob.level().getFluidState($$2).is(FluidTags.WATER)) continue;
            $$0 = $$2;
            break;
        }
        if ($$0 != null) {
            this.mob.getMoveControl().setWantedPosition($$0.getX(), $$0.getY(), $$0.getZ(), 1.0);
        }
    }
}

