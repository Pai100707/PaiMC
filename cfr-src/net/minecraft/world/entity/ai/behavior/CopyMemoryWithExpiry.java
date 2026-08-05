/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Predicate;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class CopyMemoryWithExpiry {
    public static <E extends LivingEntity, T> BehaviorControl<E> create(Predicate<E> $$0, MemoryModuleType<? extends T> $$1, MemoryModuleType<T> $$2, UniformInt $$3) {
        return BehaviorBuilder.create($$42 -> $$42.group($$42.present($$1), $$42.absent($$2)).apply((Applicative)$$42, ($$3, $$4) -> ($$5, $$6, $$7) -> {
            if (!$$0.test($$6)) {
                return false;
            }
            $$4.setWithExpiry($$42.get($$3), $$3.sample($$5.random));
            return true;
        }));
    }
}

