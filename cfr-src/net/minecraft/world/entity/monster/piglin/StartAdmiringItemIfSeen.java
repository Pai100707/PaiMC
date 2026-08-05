/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.monster.piglin;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;

public class StartAdmiringItemIfSeen {
    public static BehaviorControl<LivingEntity> create(int $$0) {
        return BehaviorBuilder.create($$1 -> $$1.group($$1.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), $$1.absent(MemoryModuleType.ADMIRING_ITEM), $$1.absent(MemoryModuleType.ADMIRING_DISABLED), $$1.absent(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)).apply((Applicative)$$1, ($$2, $$3, $$42, $$52) -> ($$4, $$5, $$6) -> {
            ItemEntity $$7 = (ItemEntity)$$1.get($$2);
            if (!PiglinAi.isLovedItem($$7.getItem())) {
                return false;
            }
            $$3.setWithExpiry(true, $$0);
            return true;
        }));
    }
}

