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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class StopAdmiringIfItemTooFarAway<E extends Piglin> {
    public static BehaviorControl<LivingEntity> create(int $$0) {
        return BehaviorBuilder.create($$1 -> $$1.group($$1.present(MemoryModuleType.ADMIRING_ITEM), $$1.registered(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)).apply((Applicative)$$1, ($$2, $$3) -> ($$4, $$5, $$6) -> {
            if (!$$5.getOffhandItem().isEmpty()) {
                return false;
            }
            Optional $$7 = $$1.tryGet($$3);
            if ($$7.isPresent() && ((ItemEntity)$$7.get()).closerThan($$5, $$0)) {
                return false;
            }
            $$2.erase();
            return true;
        }));
    }
}

