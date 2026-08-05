/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RandomStrollGoal
extends Goal {
    public static final int DEFAULT_INTERVAL = 120;
    protected final PathfinderMob mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected final double speedModifier;
    protected int interval;
    protected boolean forceTrigger;
    private final boolean checkNoActionTime;

    public RandomStrollGoal(PathfinderMob $$0, double $$1) {
        this($$0, $$1, 120);
    }

    public RandomStrollGoal(PathfinderMob $$0, double $$1, int $$2) {
        this($$0, $$1, $$2, true);
    }

    public RandomStrollGoal(PathfinderMob $$0, double $$1, int $$2, boolean $$3) {
        this.mob = $$0;
        this.speedModifier = $$1;
        this.interval = $$2;
        this.checkNoActionTime = $$3;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Vec3 $$0;
        if (this.mob.hasControllingPassenger()) {
            return false;
        }
        if (!this.forceTrigger) {
            if (this.checkNoActionTime && this.mob.getNoActionTime() >= 100) {
                return false;
            }
            if (this.mob.getRandom().nextInt(RandomStrollGoal.reducedTickDelay(this.interval)) != 0) {
                return false;
            }
        }
        if (($$0 = this.getPosition()) == null) {
            return false;
        }
        this.wantedX = $$0.x;
        this.wantedY = $$0.y;
        this.wantedZ = $$0.z;
        this.forceTrigger = false;
        return true;
    }

    protected @Nullable Vec3 getPosition() {
        return DefaultRandomPos.getPos(this.mob, 10, 7);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && !this.mob.hasControllingPassenger();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        super.stop();
    }

    public void trigger() {
        this.forceTrigger = true;
    }

    public void setInterval(int $$0) {
        this.interval = $$0;
    }
}

