/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;

public class SitWhenOrderedToGoal
extends Goal {
    private final TamableAnimal mob;

    public SitWhenOrderedToGoal(TamableAnimal $$0) {
        this.mob = $$0;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.isOrderedToSit();
    }

    @Override
    public boolean canUse() {
        boolean $$0 = this.mob.isOrderedToSit();
        if (!$$0 && !this.mob.isTame()) {
            return false;
        }
        if (this.mob.isInWater()) {
            return false;
        }
        if (!this.mob.onGround()) {
            return false;
        }
        LivingEntity $$1 = this.mob.getOwner();
        if ($$1 == null || $$1.level() != this.mob.level()) {
            return true;
        }
        if (this.mob.distanceToSqr($$1) < 144.0 && $$1.getLastHurtByMob() != null) {
            return false;
        }
        return $$0;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.mob.setInSittingPose(true);
    }

    @Override
    public void stop() {
        this.mob.setInSittingPose(false);
    }
}

