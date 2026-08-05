/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class UpdateActivityFromSchedule {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create($$02 -> $$02.point(($$0, $$1, $$2) -> {
            $$1.getBrain().updateActivityFromSchedule($$0.environmentAttributes(), $$0.getGameTime(), $$1.position());
            return true;
        }));
    }
}

