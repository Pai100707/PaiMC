/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class EraseMemoryIf {
    public static <E extends LivingEntity> BehaviorControl<E> create(Predicate<E> $$0, MemoryModuleType<?> $$1) {
        return BehaviorBuilder.create($$2 -> $$2.group($$2.present($$1)).apply((Applicative)$$2, $$1 -> ($$2, $$3, $$4) -> {
            if ($$0.test($$3)) {
                $$1.erase();
                return true;
            }
            return false;
        }));
    }
}

