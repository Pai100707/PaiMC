/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromLookTarget {
    public static OneShot<LivingEntity> create(float $$02, int $$12) {
        return SetWalkTargetFromLookTarget.create($$0 -> true, $$1 -> Float.valueOf($$02), $$12);
    }

    public static OneShot<LivingEntity> create(Predicate<LivingEntity> $$0, Function<LivingEntity, Float> $$1, int $$2) {
        return BehaviorBuilder.create($$3 -> $$3.group($$3.absent(MemoryModuleType.WALK_TARGET), $$3.present(MemoryModuleType.LOOK_TARGET)).apply((Applicative)$$3, ($$4, $$5) -> ($$6, $$7, $$8) -> {
            if (!$$0.test($$7)) {
                return false;
            }
            $$4.set(new WalkTarget((PositionTracker)$$3.get($$5), ((Float)$$1.apply($$7)).floatValue(), $$2));
            return true;
        }));
    }
}

