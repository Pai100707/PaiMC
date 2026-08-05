/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerCalmDown {
    private static final int SAFE_DISTANCE_FROM_DANGER = 36;

    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create($$0 -> $$0.group($$0.registered(MemoryModuleType.HURT_BY), $$0.registered(MemoryModuleType.HURT_BY_ENTITY), $$0.registered(MemoryModuleType.NEAREST_HOSTILE)).apply((Applicative)$$0, ($$1, $$2, $$3) -> ($$4, $$5, $$6) -> {
            boolean $$7;
            boolean bl = $$7 = $$0.tryGet($$1).isPresent() || $$0.tryGet($$3).isPresent() || $$0.tryGet($$2).filter($$1 -> $$1.distanceToSqr($$5) <= 36.0).isPresent();
            if (!$$7) {
                $$1.erase();
                $$2.erase();
                $$5.getBrain().updateActivityFromSchedule($$4.environmentAttributes(), $$4.getGameTime(), $$5.position());
            }
            return true;
        }));
    }
}

