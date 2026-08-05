/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior.warden;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class SetRoarTarget {
    public static <E extends Warden> BehaviorControl<E> create(Function<E, Optional<? extends LivingEntity>> $$0) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> $$12) -> $$12.group($$12.absent(MemoryModuleType.ROAR_TARGET), $$12.absent(MemoryModuleType.ATTACK_TARGET), $$12.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply((Applicative)$$12, ($$1, $$2, $$32) -> ($$3, $$4, $$5) -> {
            Optional $$6 = (Optional)$$0.apply($$4);
            if ($$6.filter($$4::canTargetEntity).isEmpty()) {
                return false;
            }
            $$1.set((LivingEntity)$$6.get());
            $$32.erase();
            return true;
        }));
    }
}

