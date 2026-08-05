/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class StayCloseToTarget {
    public static BehaviorControl<LivingEntity> create(Function<LivingEntity, Optional<PositionTracker>> $$0, Predicate<LivingEntity> $$1, int $$2, int $$3, float $$4) {
        return BehaviorBuilder.create($$52 -> $$52.group($$52.registered(MemoryModuleType.LOOK_TARGET), $$52.registered(MemoryModuleType.WALK_TARGET)).apply((Applicative)$$52, ($$5, $$6) -> ($$7, $$8, $$9) -> {
            Optional $$10 = (Optional)$$0.apply($$8);
            if ($$10.isEmpty() || !$$1.test($$8)) {
                return false;
            }
            PositionTracker $$11 = (PositionTracker)$$10.get();
            if ($$8.position().closerThan($$11.currentPosition(), $$3)) {
                return false;
            }
            PositionTracker $$12 = (PositionTracker)$$10.get();
            $$5.set($$12);
            $$6.set(new WalkTarget($$12, $$4, $$2));
            return true;
        }));
    }
}

