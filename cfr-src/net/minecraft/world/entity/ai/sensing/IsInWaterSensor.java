/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class IsInWaterSensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.IS_IN_WATER);
    }

    @Override
    protected void doTick(ServerLevel $$0, LivingEntity $$1) {
        if ($$1.isInWater()) {
            $$1.getBrain().setMemory(MemoryModuleType.IS_IN_WATER, Unit.INSTANCE);
        } else {
            $$1.getBrain().eraseMemory(MemoryModuleType.IS_IN_WATER);
        }
    }
}

