/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class OcelotAttackGoal
extends Goal {
    private final Mob mob;
    private LivingEntity target;
    private int attackTime;

    public OcelotAttackGoal(Mob $$0) {
        this.mob = $$0;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity $$0 = this.mob.getTarget();
        if ($$0 == null) {
            return false;
        }
        this.target = $$0;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.target.isAlive()) {
            return false;
        }
        if (this.mob.distanceToSqr(this.target) > 225.0) {
            return false;
        }
        return !this.mob.getNavigation().isDone() || this.canUse();
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        double $$0 = this.mob.getBbWidth() * 2.0f * (this.mob.getBbWidth() * 2.0f);
        double $$1 = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        double $$2 = 0.8;
        if ($$1 > $$0 && $$1 < 16.0) {
            $$2 = 1.33;
        } else if ($$1 < 225.0) {
            $$2 = 0.6;
        }
        this.mob.getNavigation().moveTo(this.target, $$2);
        this.attackTime = Math.max(this.attackTime - 1, 0);
        if ($$1 > $$0) {
            return;
        }
        if (this.attackTime > 0) {
            return;
        }
        this.attackTime = 20;
        this.mob.doHurtTarget(OcelotAttackGoal.getServerLevel(this.mob), this.target);
    }
}

