/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public abstract class Behavior<E extends LivingEntity>
implements BehaviorControl<E> {
    public static final int DEFAULT_DURATION = 60;
    protected final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
    private Status status = Status.STOPPED;
    private long endTimestamp;
    private final int minDuration;
    private final int maxDuration;

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> $$0) {
        this($$0, 60);
    }

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> $$0, int $$1) {
        this($$0, $$1, $$1);
    }

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> $$0, int $$1, int $$2) {
        this.minDuration = $$1;
        this.maxDuration = $$2;
        this.entryCondition = $$0;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public final boolean tryStart(ServerLevel $$0, E $$1, long $$2) {
        if (this.hasRequiredMemories($$1) && this.checkExtraStartConditions($$0, $$1)) {
            this.status = Status.RUNNING;
            int $$3 = this.minDuration + $$0.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
            this.endTimestamp = $$2 + (long)$$3;
            this.start($$0, $$1, $$2);
            return true;
        }
        return false;
    }

    protected void start(ServerLevel $$0, E $$1, long $$2) {
    }

    @Override
    public final void tickOrStop(ServerLevel $$0, E $$1, long $$2) {
        if (!this.timedOut($$2) && this.canStillUse($$0, $$1, $$2)) {
            this.tick($$0, $$1, $$2);
        } else {
            this.doStop($$0, $$1, $$2);
        }
    }

    protected void tick(ServerLevel $$0, E $$1, long $$2) {
    }

    @Override
    public final void doStop(ServerLevel $$0, E $$1, long $$2) {
        this.status = Status.STOPPED;
        this.stop($$0, $$1, $$2);
    }

    protected void stop(ServerLevel $$0, E $$1, long $$2) {
    }

    protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
        return false;
    }

    protected boolean timedOut(long $$0) {
        return $$0 > this.endTimestamp;
    }

    protected boolean checkExtraStartConditions(ServerLevel $$0, E $$1) {
        return true;
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }

    protected boolean hasRequiredMemories(E $$0) {
        for (Map.Entry<MemoryModuleType<?>, MemoryStatus> $$1 : this.entryCondition.entrySet()) {
            MemoryModuleType<?> $$2 = $$1.getKey();
            MemoryStatus $$3 = $$1.getValue();
            if (((LivingEntity)$$0).getBrain().checkMemory($$2, $$3)) continue;
            return false;
        }
        return true;
    }

    public static enum Status {
        STOPPED,
        RUNNING;

    }
}

