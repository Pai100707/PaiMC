/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public abstract class TargetGoal
extends Goal {
    private static final int EMPTY_REACH_CACHE = 0;
    private static final int CAN_REACH_CACHE = 1;
    private static final int CANT_REACH_CACHE = 2;
    protected final Mob mob;
    protected final boolean mustSee;
    private final boolean mustReach;
    private int reachCache;
    private int reachCacheTime;
    private int unseenTicks;
    protected @Nullable LivingEntity targetMob;
    protected int unseenMemoryTicks = 60;

    public TargetGoal(Mob $$0, boolean $$1) {
        this($$0, $$1, false);
    }

    public TargetGoal(Mob $$0, boolean $$1, boolean $$2) {
        this.mob = $$0;
        this.mustSee = $$1;
        this.mustReach = $$2;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity $$0 = this.mob.getTarget();
        if ($$0 == null) {
            $$0 = this.targetMob;
        }
        if ($$0 == null) {
            return false;
        }
        if (!this.mob.canAttack($$0)) {
            return false;
        }
        PlayerTeam $$1 = this.mob.getTeam();
        PlayerTeam $$2 = $$0.getTeam();
        if ($$1 != null && $$2 == $$1) {
            return false;
        }
        double $$3 = this.getFollowDistance();
        if (this.mob.distanceToSqr($$0) > $$3 * $$3) {
            return false;
        }
        if (this.mustSee) {
            if (this.mob.getSensing().hasLineOfSight($$0)) {
                this.unseenTicks = 0;
            } else if (++this.unseenTicks > TargetGoal.reducedTickDelay(this.unseenMemoryTicks)) {
                return false;
            }
        }
        this.mob.setTarget($$0);
        return true;
    }

    protected double getFollowDistance() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void start() {
        this.reachCache = 0;
        this.reachCacheTime = 0;
        this.unseenTicks = 0;
    }

    @Override
    public void stop() {
        this.mob.setTarget(null);
        this.targetMob = null;
    }

    protected boolean canAttack(@Nullable LivingEntity $$0, TargetingConditions $$1) {
        if ($$0 == null) {
            return false;
        }
        if (!$$1.test(TargetGoal.getServerLevel(this.mob), this.mob, $$0)) {
            return false;
        }
        if (!this.mob.isWithinHome($$0.blockPosition())) {
            return false;
        }
        if (this.mustReach) {
            if (--this.reachCacheTime <= 0) {
                this.reachCache = 0;
            }
            if (this.reachCache == 0) {
                int n = this.reachCache = this.canReach($$0) ? 1 : 2;
            }
            if (this.reachCache == 2) {
                return false;
            }
        }
        return true;
    }

    private boolean canReach(LivingEntity $$0) {
        int $$4;
        this.reachCacheTime = TargetGoal.reducedTickDelay(10 + this.mob.getRandom().nextInt(5));
        Path $$1 = this.mob.getNavigation().createPath($$0, 0);
        if ($$1 == null) {
            return false;
        }
        Node $$2 = $$1.getEndNode();
        if ($$2 == null) {
            return false;
        }
        int $$3 = $$2.x - $$0.getBlockX();
        return (double)($$3 * $$3 + ($$4 = $$2.z - $$0.getBlockZ()) * $$4) <= 2.25;
    }

    public TargetGoal setUnseenMemoryTicks(int $$0) {
        this.unseenMemoryTicks = $$0;
        return this;
    }
}

