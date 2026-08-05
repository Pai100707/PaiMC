/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Function;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class BabyFollowAdult {
    public static OneShot<LivingEntity> create(UniformInt $$0, float $$12) {
        return BabyFollowAdult.create($$0, $$1 -> Float.valueOf($$12), MemoryModuleType.NEAREST_VISIBLE_ADULT, false);
    }

    public static OneShot<LivingEntity> create(UniformInt $$0, Function<LivingEntity, Float> $$1, MemoryModuleType<? extends LivingEntity> $$2, boolean $$3) {
        return BehaviorBuilder.create($$42 -> $$42.group($$42.present($$2), $$42.registered(MemoryModuleType.LOOK_TARGET), $$42.absent(MemoryModuleType.WALK_TARGET)).apply((Applicative)$$42, ($$4, $$5, $$6) -> ($$7, $$8, $$9) -> {
            if (!$$8.isBaby()) {
                return false;
            }
            LivingEntity $$10 = (LivingEntity)$$42.get($$4);
            if ($$8.closerThan($$10, $$0.getMaxValue() + 1) && !$$8.closerThan($$10, $$0.getMinValue())) {
                WalkTarget $$11 = new WalkTarget(new EntityTracker($$10, $$3, $$3), ((Float)$$1.apply($$8)).floatValue(), $$0.getMinValue() - 1);
                $$5.set(new EntityTracker($$10, true, $$3));
                $$6.set($$11);
                return true;
            }
            return false;
        }));
    }
}

