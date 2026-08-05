/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BreathAirGoal
extends Goal {
    private final PathfinderMob mob;

    public BreathAirGoal(PathfinderMob $$0) {
        this.mob = $$0;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getAirSupply() < 140;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        this.findAirPosition();
    }

    private void findAirPosition() {
        Iterable<BlockPos> $$0 = BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 1.0), this.mob.getBlockY(), Mth.floor(this.mob.getZ() - 1.0), Mth.floor(this.mob.getX() + 1.0), Mth.floor(this.mob.getY() + 8.0), Mth.floor(this.mob.getZ() + 1.0));
        Vec3i $$1 = null;
        for (BlockPos $$2 : $$0) {
            if (!this.givesAir(this.mob.level(), $$2)) continue;
            $$1 = $$2;
            break;
        }
        if ($$1 == null) {
            $$1 = BlockPos.containing(this.mob.getX(), this.mob.getY() + 8.0, this.mob.getZ());
        }
        this.mob.getNavigation().moveTo($$1.getX(), $$1.getY() + 1, $$1.getZ(), 1.0);
    }

    @Override
    public void tick() {
        this.findAirPosition();
        this.mob.moveRelative(0.02f, new Vec3(this.mob.xxa, this.mob.yya, this.mob.zza));
        this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
    }

    private boolean givesAir(LevelReader $$0, BlockPos $$1) {
        BlockState $$2 = $$0.getBlockState($$1);
        return ($$0.getFluidState($$1).isEmpty() || $$2.is(Blocks.BUBBLE_COLUMN)) && $$2.isPathfindable(PathComputationType.LAND);
    }
}

