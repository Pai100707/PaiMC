/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.monster.piglin;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;

public class RememberIfHoglinWasKilled {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create($$0 -> $$0.group($$0.present(MemoryModuleType.ATTACK_TARGET), $$0.registered(MemoryModuleType.HUNTED_RECENTLY)).apply((Applicative)$$0, ($$1, $$2) -> ($$3, $$4, $$5) -> {
            LivingEntity $$6 = (LivingEntity)$$0.get($$1);
            if ($$6.getType() == EntityType.HOGLIN && $$6.isDeadOrDying()) {
                $$2.setWithExpiry(true, PiglinAi.TIME_BETWEEN_HUNTS.sample($$4.level().random));
            }
            return true;
        }));
    }
}

