/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class WaterBoundPathNavigation
extends PathNavigation {
    private boolean allowBreaching;

    public WaterBoundPathNavigation(Mob $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    protected PathFinder createPathFinder(int $$0) {
        this.allowBreaching = this.mob.getType() == EntityType.DOLPHIN;
        this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching);
        this.nodeEvaluator.setCanPassDoors(false);
        return new PathFinder(this.nodeEvaluator, $$0);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.allowBreaching || this.mob.isInLiquid();
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
        return WaterBoundPathNavigation.isClearForMovementBetween(this.mob, $$0, $$1, false);
    }

    @Override
    public boolean isStableDestination(BlockPos $$0) {
        return !this.level.getBlockState($$0).isSolidRender();
    }

    @Override
    public void setCanFloat(boolean $$0) {
    }

    @Override
    public boolean canNavigateGround() {
        return false;
    }
}

