/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.sensing;

import java.util.Optional;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.AdultSensor;

public class AdultSensorAnyType
extends AdultSensor {
    @Override
    protected void setNearestVisibleAdult(LivingEntity $$02, NearestVisibleLivingEntities $$1) {
        Optional<LivingEntity> $$2 = $$1.findClosest($$0 -> $$0.getType().is(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS) && !$$0.isBaby());
        $$02.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, $$2);
    }
}

