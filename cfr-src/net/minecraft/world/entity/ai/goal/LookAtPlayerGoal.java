/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class LookAtPlayerGoal
extends Goal {
    public static final float DEFAULT_PROBABILITY = 0.02f;
    protected final Mob mob;
    protected @Nullable Entity lookAt;
    protected final float lookDistance;
    private int lookTime;
    protected final float probability;
    private final boolean onlyHorizontal;
    protected final Class<? extends LivingEntity> lookAtType;
    protected final TargetingConditions lookAtContext;

    public LookAtPlayerGoal(Mob $$0, Class<? extends LivingEntity> $$1, float $$2) {
        this($$0, $$1, $$2, 0.02f);
    }

    public LookAtPlayerGoal(Mob $$0, Class<? extends LivingEntity> $$1, float $$2, float $$3) {
        this($$0, $$1, $$2, $$3, false);
    }

    public LookAtPlayerGoal(Mob $$0, Class<? extends LivingEntity> $$12, float $$22, float $$3, boolean $$4) {
        this.mob = $$0;
        this.lookAtType = $$12;
        this.lookDistance = $$22;
        this.probability = $$3;
        this.onlyHorizontal = $$4;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        if ($$12 == Player.class) {
            Predicate<Entity> $$5 = EntitySelector.notRiding($$0);
            this.lookAtContext = TargetingConditions.forNonCombat().range($$22).selector(($$1, $$2) -> $$5.test($$1));
        } else {
            this.lookAtContext = TargetingConditions.forNonCombat().range($$22);
        }
    }

    @Override
    public boolean canUse() {
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
        }
        if (this.mob.getTarget() != null) {
            this.lookAt = this.mob.getTarget();
        }
        ServerLevel $$02 = LookAtPlayerGoal.getServerLevel(this.mob);
        this.lookAt = this.lookAtType == Player.class ? $$02.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()) : $$02.getNearestEntity(this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate(this.lookDistance, 3.0, this.lookDistance), $$0 -> true), this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        return this.lookAt != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.lookAt.isAlive()) {
            return false;
        }
        if (this.mob.distanceToSqr(this.lookAt) > (double)(this.lookDistance * this.lookDistance)) {
            return false;
        }
        return this.lookTime > 0;
    }

    @Override
    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public void tick() {
        if (!this.lookAt.isAlive()) {
            return;
        }
        double $$0 = this.onlyHorizontal ? this.mob.getEyeY() : this.lookAt.getEyeY();
        this.mob.getLookControl().setLookAt(this.lookAt.getX(), $$0, this.lookAt.getZ());
        --this.lookTime;
    }
}

