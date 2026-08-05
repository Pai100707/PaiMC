/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

public class DoNothing
implements BehaviorControl<LivingEntity> {
    private final int minDuration;
    private final int maxDuration;
    private Behavior.Status status = Behavior.Status.STOPPED;
    private long endTimestamp;

    public DoNothing(int $$0, int $$1) {
        this.minDuration = $$0;
        this.maxDuration = $$1;
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public final boolean tryStart(ServerLevel $$0, LivingEntity $$1, long $$2) {
        this.status = Behavior.Status.RUNNING;
        int $$3 = this.minDuration + $$0.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
        this.endTimestamp = $$2 + (long)$$3;
        return true;
    }

    @Override
    public final void tickOrStop(ServerLevel $$0, LivingEntity $$1, long $$2) {
        if ($$2 > this.endTimestamp) {
            this.doStop($$0, $$1, $$2);
        }
    }

    @Override
    public final void doStop(ServerLevel $$0, LivingEntity $$1, long $$2) {
        this.status = Behavior.Status.STOPPED;
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }
}

