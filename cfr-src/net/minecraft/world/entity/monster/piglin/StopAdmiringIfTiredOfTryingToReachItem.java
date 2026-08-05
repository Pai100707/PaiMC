/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.monster.piglin;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAdmiringIfTiredOfTryingToReachItem {
    public static BehaviorControl<LivingEntity> create(int $$0, int $$1) {
        return BehaviorBuilder.create($$2 -> $$2.group($$2.present(MemoryModuleType.ADMIRING_ITEM), $$2.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), $$2.registered(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM), $$2.registered(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)).apply((Applicative)$$2, ($$3, $$4, $$5, $$62) -> ($$6, $$7, $$8) -> {
            if (!$$7.getOffhandItem().isEmpty()) {
                return false;
            }
            Optional $$9 = $$2.tryGet($$5);
            if ($$9.isEmpty()) {
                $$5.set(0);
            } else {
                int $$10 = (Integer)$$9.get();
                if ($$10 > $$0) {
                    $$3.erase();
                    $$5.erase();
                    $$62.setWithExpiry(true, $$1);
                } else {
                    $$5.set($$10 + 1);
                }
            }
            return true;
        }));
    }
}

