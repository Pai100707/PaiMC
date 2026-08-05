/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.schedule.Activity;

public class WakeUp {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create($$02 -> $$02.point(($$0, $$1, $$2) -> {
            if ($$1.getBrain().isActive(Activity.REST) || !$$1.isSleeping()) {
                return false;
            }
            $$1.stopSleeping();
            return true;
        }));
    }
}

