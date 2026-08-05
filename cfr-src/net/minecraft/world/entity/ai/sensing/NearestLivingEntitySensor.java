/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;

public class NearestLivingEntitySensor<T extends LivingEntity>
extends Sensor<T> {
    @Override
    protected void doTick(ServerLevel $$0, T $$12) {
        double $$2 = ((LivingEntity)$$12).getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB $$3 = ((Entity)$$12).getBoundingBox().inflate($$2, $$2, $$2);
        List<LivingEntity> $$4 = $$0.getEntitiesOfClass(LivingEntity.class, $$3, $$1 -> $$1 != $$12 && $$1.isAlive());
        $$4.sort(Comparator.comparingDouble(arg_0 -> $$12.distanceToSqr(arg_0)));
        Brain<?> $$5 = ((LivingEntity)$$12).getBrain();
        $$5.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, $$4);
        $$5.setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new NearestVisibleLivingEntities($$0, (LivingEntity)$$12, $$4));
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }
}

