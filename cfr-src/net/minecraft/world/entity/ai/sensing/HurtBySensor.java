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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class HurtBySensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY);
    }

    @Override
    protected void doTick(ServerLevel $$0, LivingEntity $$1) {
        Brain<?> $$22 = $$1.getBrain();
        DamageSource $$3 = $$1.getLastDamageSource();
        if ($$3 != null) {
            $$22.setMemory(MemoryModuleType.HURT_BY, $$1.getLastDamageSource());
            Entity $$4 = $$3.getEntity();
            if ($$4 instanceof LivingEntity) {
                $$22.setMemory(MemoryModuleType.HURT_BY_ENTITY, (LivingEntity)$$4);
            }
        } else {
            $$22.eraseMemory(MemoryModuleType.HURT_BY);
        }
        $$22.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent($$2 -> {
            if (!$$2.isAlive() || $$2.level() != $$0) {
                $$22.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
            }
        });
    }
}

