/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.animal.axolotl;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class ValidatePlayDead {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create($$0 -> $$0.group($$0.present(MemoryModuleType.PLAY_DEAD_TICKS), $$0.registered(MemoryModuleType.HURT_BY_ENTITY)).apply((Applicative)$$0, ($$1, $$2) -> ($$3, $$4, $$5) -> {
            int $$6 = (Integer)$$0.get($$1);
            if ($$6 <= 0) {
                $$1.erase();
                $$2.erase();
                $$4.getBrain().useDefaultActivity();
            } else {
                $$1.set($$6 - 1);
            }
            return true;
        }));
    }
}

