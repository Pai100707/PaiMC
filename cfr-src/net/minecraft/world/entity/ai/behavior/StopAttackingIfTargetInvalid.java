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

public class StopAttackingIfTargetInvalid {
    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

    public static <E extends Mob> BehaviorControl<E> create(TargetErasedCallback<E> $$02) {
        return StopAttackingIfTargetInvalid.create(($$0, $$1) -> false, $$02, true);
    }

    public static <E extends Mob> BehaviorControl<E> create(StopAttackCondition $$02) {
        return StopAttackingIfTargetInvalid.create($$02, ($$0, $$1, $$2) -> {}, true);
    }

    public static <E extends Mob> BehaviorControl<E> create() {
        return StopAttackingIfTargetInvalid.create(($$0, $$1) -> false, ($$0, $$1, $$2) -> {}, true);
    }

    public static <E extends Mob> BehaviorControl<E> create(StopAttackCondition $$0, TargetErasedCallback<E> $$1, boolean $$2) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> $$3) -> $$3.group($$3.present(MemoryModuleType.ATTACK_TARGET), $$3.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply((Applicative)$$3, ($$4, $$5) -> ($$6, $$7, $$8) -> {
            LivingEntity $$9 = (LivingEntity)$$3.get($$4);
            if (!$$7.canAttack($$9) || $$2 && StopAttackingIfTargetInvalid.isTiredOfTryingToReachTarget($$7, $$3.tryGet($$5)) || !$$9.isAlive() || $$9.level() != $$7.level() || $$0.test($$6, $$9)) {
                $$1.accept($$6, $$7, $$9);
                $$4.erase();
                return true;
            }
            return true;
        }));
    }

    private static boolean isTiredOfTryingToReachTarget(LivingEntity $$0, Optional<Long> $$1) {
        return $$1.isPresent() && $$0.level().getGameTime() - $$1.get() > 200L;
    }

    @FunctionalInterface
    public static interface StopAttackCondition {
        public boolean test(ServerLevel var1, LivingEntity var2);
    }

    @FunctionalInterface
    public static interface TargetErasedCallback<E> {
        public void accept(ServerLevel var1, E var2, LivingEntity var3);
    }
}

