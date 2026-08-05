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

public class AdultSensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    @Override
    protected void doTick(ServerLevel $$0, LivingEntity $$12) {
        $$12.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent($$1 -> this.setNearestVisibleAdult($$12, (NearestVisibleLivingEntities)$$1));
    }

    protected void setNearestVisibleAdult(LivingEntity $$0, NearestVisibleLivingEntities $$12) {
        Optional<LivingEntity> $$2 = $$12.findClosest($$1 -> $$1.getType() == $$0.getType() && !$$1.isBaby());
        $$0.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, $$2);
    }
}

