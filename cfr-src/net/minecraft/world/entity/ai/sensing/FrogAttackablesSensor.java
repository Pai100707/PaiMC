/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.sensing;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.frog.Frog;

public class FrogAttackablesSensor
extends NearestVisibleLivingEntitySensor {
    public static final float TARGET_DETECTION_DISTANCE = 10.0f;

    @Override
    protected boolean isMatchingEntity(ServerLevel $$0, LivingEntity $$1, LivingEntity $$2) {
        if (!$$1.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && Sensor.isEntityAttackable($$0, $$1, $$2) && Frog.canEat($$2) && !this.isUnreachableAttackTarget($$1, $$2)) {
            return $$2.closerThan($$1, 10.0);
        }
        return false;
    }

    private boolean isUnreachableAttackTarget(LivingEntity $$0, LivingEntity $$1) {
        List $$2 = $$0.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
        return $$2.contains($$1.getUUID());
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }
}

