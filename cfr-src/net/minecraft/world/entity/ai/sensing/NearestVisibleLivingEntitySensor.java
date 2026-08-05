/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

public abstract class NearestVisibleLivingEntitySensor
extends Sensor<LivingEntity> {
    protected abstract boolean isMatchingEntity(ServerLevel var1, LivingEntity var2, LivingEntity var3);

    protected abstract MemoryModuleType<LivingEntity> getMemory();

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(this.getMemory());
    }

    @Override
    protected void doTick(ServerLevel $$0, LivingEntity $$1) {
        $$1.getBrain().setMemory(this.getMemory(), this.getNearestEntity($$0, $$1));
    }

    private Optional<LivingEntity> getNearestEntity(ServerLevel $$0, LivingEntity $$1) {
        return this.getVisibleEntities($$1).flatMap($$22 -> $$22.findClosest($$2 -> this.isMatchingEntity($$0, $$1, (LivingEntity)$$2)));
    }

    protected Optional<NearestVisibleLivingEntities> getVisibleEntities(LivingEntity $$0) {
        return $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }
}

