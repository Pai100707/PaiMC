/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

public class FollowOwnerGoal
extends Goal {
    private final TamableAnimal tamable;
    private @Nullable LivingEntity owner;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;

    public FollowOwnerGoal(TamableAnimal $$0, double $$1, float $$2, float $$3) {
        this.tamable = $$0;
        this.speedModifier = $$1;
        this.navigation = $$0.getNavigation();
        this.startDistance = $$2;
        this.stopDistance = $$3;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!($$0.getNavigation() instanceof GroundPathNavigation) && !($$0.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity $$0 = this.tamable.getOwner();
        if ($$0 == null) {
            return false;
        }
        if (this.tamable.unableToMoveToOwner()) {
            return false;
        }
        if (this.tamable.distanceToSqr($$0) < (double)(this.startDistance * this.startDistance)) {
            return false;
        }
        this.owner = $$0;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        }
        if (this.tamable.unableToMoveToOwner()) {
            return false;
        }
        return !(this.tamable.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamable.getPathfindingMalus(PathType.WATER);
        this.tamable.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tamable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        boolean $$0 = this.tamable.shouldTryTeleportToOwner();
        if (!$$0) {
            this.tamable.getLookControl().setLookAt(this.owner, 10.0f, this.tamable.getMaxHeadXRot());
        }
        if (--this.timeToRecalcPath > 0) {
            return;
        }
        this.timeToRecalcPath = this.adjustedTickDelay(10);
        if ($$0) {
            this.tamable.tryToTeleportToOwner();
        } else {
            this.navigation.moveTo(this.owner, this.speedModifier);
        }
    }
}

