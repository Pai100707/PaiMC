/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class AmphibiousPathNavigation
extends PathNavigation {
    public AmphibiousPathNavigation(Mob $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    protected PathFinder createPathFinder(int $$0) {
        this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
        return new PathFinder(this.nodeEvaluator, $$0);
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(0.5), this.mob.getZ());
    }

    @Override
    protected double getGroundY(Vec3 $$0) {
        return $$0.y;
    }

    @Override
    protected boolean canMoveDirectly(Vec3 $$0, Vec3 $$1) {
        if (this.mob.isInLiquid()) {
            return AmphibiousPathNavigation.isClearForMovementBetween(this.mob, $$0, $$1, false);
        }
        return false;
    }

    @Override
    public boolean isStableDestination(BlockPos $$0) {
        return !this.level.getBlockState($$0.below()).isAir();
    }

    @Override
    public void setCanFloat(boolean $$0) {
    }

    @Override
    public boolean canNavigateGround() {
        return true;
    }
}

