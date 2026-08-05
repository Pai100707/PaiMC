/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StartAttacking {
    public static <E extends Mob> BehaviorControl<E> create(TargetFinder<E> $$02) {
        return StartAttacking.create(($$0, $$1) -> true, $$02);
    }

    public static <E extends Mob> BehaviorControl<E> create(StartAttackingCondition<E> $$0, TargetFinder<E> $$1) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> $$22) -> $$22.group($$22.absent(MemoryModuleType.ATTACK_TARGET), $$22.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply((Applicative)$$22, ($$2, $$3) -> ($$4, $$5, $$6) -> {
            if (!$$0.test($$4, $$5)) {
                return false;
            }
            Optional<LivingEntity> $$7 = $$1.get($$4, $$5);
            if ($$7.isEmpty()) {
                return false;
            }
            LivingEntity $$8 = $$7.get();
            if (!$$5.canAttack($$8)) {
                return false;
            }
            $$2.set($$8);
            $$3.erase();
            return true;
        }));
    }

    @FunctionalInterface
    public static interface StartAttackingCondition<E> {
        public boolean test(ServerLevel var1, E var2);
    }

    @FunctionalInterface
    public static interface TargetFinder<E> {
        public Optional<? extends LivingEntity> get(ServerLevel var1, E var2);
    }
}

