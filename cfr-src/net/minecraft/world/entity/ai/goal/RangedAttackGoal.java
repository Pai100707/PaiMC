/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jspecify.annotations.Nullable;

public class RangedAttackGoal
extends Goal {
    private final Mob mob;
    private final RangedAttackMob rangedAttackMob;
    private @Nullable LivingEntity target;
    private int attackTime = -1;
    private final double speedModifier;
    private int seeTime;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;

    public RangedAttackGoal(RangedAttackMob $$0, double $$1, int $$2, float $$3) {
        this($$0, $$1, $$2, $$2, $$3);
    }

    public RangedAttackGoal(RangedAttackMob $$0, double $$1, int $$2, int $$3, float $$4) {
        if (!($$0 instanceof LivingEntity)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        this.rangedAttackMob = $$0;
        this.mob = (Mob)((Object)$$0);
        this.speedModifier = $$1;
        this.attackIntervalMin = $$2;
        this.attackIntervalMax = $$3;
        this.attackRadius = $$4;
        this.attackRadiusSqr = $$4 * $$4;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity $$0 = this.mob.getTarget();
        if ($$0 == null || !$$0.isAlive()) {
            return false;
        }
        this.target = $$0;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || this.target.isAlive() && !this.mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        double $$0 = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean $$1 = this.mob.getSensing().hasLineOfSight(this.target);
        this.seeTime = $$1 ? ++this.seeTime : 0;
        if ($$0 > (double)this.attackRadiusSqr || this.seeTime < 5) {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier);
        } else {
            this.mob.getNavigation().stop();
        }
        this.mob.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        if (--this.attackTime == 0) {
            if (!$$1) {
                return;
            }
            float $$2 = (float)Math.sqrt($$0) / this.attackRadius;
            float $$3 = Mth.clamp($$2, 0.1f, 1.0f);
            this.rangedAttackMob.performRangedAttack(this.target, $$3);
            this.attackTime = Mth.floor($$2 * (float)(this.attackIntervalMax - this.attackIntervalMin) + (float)this.attackIntervalMin);
        } else if (this.attackTime < 0) {
            this.attackTime = Mth.floor(Mth.lerp(Math.sqrt($$0) / (double)this.attackRadius, (double)this.attackIntervalMin, (double)this.attackIntervalMax));
        }
    }
}

