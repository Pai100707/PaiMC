/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.frog.Frog;

public class Croak
extends Behavior<Frog> {
    private static final int CROAK_TICKS = 60;
    private static final int TIME_OUT_DURATION = 100;
    private int croakCounter;

    public Croak() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), 100);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Frog $$1) {
        return $$1.getPose() == Pose.STANDING;
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Frog $$1, long $$2) {
        return this.croakCounter < 60;
    }

    @Override
    protected void start(ServerLevel $$0, Frog $$1, long $$2) {
        if ($$1.isInLiquid()) {
            return;
        }
        $$1.setPose(Pose.CROAKING);
        this.croakCounter = 0;
    }

    @Override
    protected void stop(ServerLevel $$0, Frog $$1, long $$2) {
        $$1.setPose(Pose.STANDING);
    }

    @Override
    protected void tick(ServerLevel $$0, Frog $$1, long $$2) {
        ++this.croakCounter;
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Frog)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Frog)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Frog)livingEntity, l);
    }
}

