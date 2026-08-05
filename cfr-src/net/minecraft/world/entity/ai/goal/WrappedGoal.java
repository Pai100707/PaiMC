/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;

public class WrappedGoal
extends Goal {
    private final Goal goal;
    private final int priority;
    private boolean isRunning;

    public WrappedGoal(int $$0, Goal $$1) {
        this.priority = $$0;
        this.goal = $$1;
    }

    public boolean canBeReplacedBy(WrappedGoal $$0) {
        return this.isInterruptable() && $$0.getPriority() < this.getPriority();
    }

    @Override
    public boolean canUse() {
        return this.goal.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.goal.canContinueToUse();
    }

    @Override
    public boolean isInterruptable() {
        return this.goal.isInterruptable();
    }

    @Override
    public void start() {
        if (this.isRunning) {
            return;
        }
        this.isRunning = true;
        this.goal.start();
    }

    @Override
    public void stop() {
        if (!this.isRunning) {
            return;
        }
        this.isRunning = false;
        this.goal.stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return this.goal.requiresUpdateEveryTick();
    }

    @Override
    protected int adjustedTickDelay(int $$0) {
        return this.goal.adjustedTickDelay($$0);
    }

    @Override
    public void tick() {
        this.goal.tick();
    }

    @Override
    public void setFlags(EnumSet<Goal.Flag> $$0) {
        this.goal.setFlags($$0);
    }

    @Override
    public EnumSet<Goal.Flag> getFlags() {
        return this.goal.getFlags();
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public int getPriority() {
        return this.priority;
    }

    public Goal getGoal() {
        return this.goal;
    }

    public boolean equals(@Nullable Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 == null || this.getClass() != $$0.getClass()) {
            return false;
        }
        return this.goal.equals(((WrappedGoal)$$0).goal);
    }

    public int hashCode() {
        return this.goal.hashCode();
    }
}

