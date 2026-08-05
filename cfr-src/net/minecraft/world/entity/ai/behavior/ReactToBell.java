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
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ReactToBell {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create($$0 -> $$0.group($$0.present(MemoryModuleType.HEARD_BELL_TIME)).apply((Applicative)$$0, $$02 -> ($$0, $$1, $$2) -> {
            Raid $$3 = $$0.getRaidAt($$1.blockPosition());
            if ($$3 == null) {
                $$1.getBrain().setActiveActivityIfPossible(Activity.HIDE);
            }
            return true;
        }));
    }
}

