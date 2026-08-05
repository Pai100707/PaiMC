/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetEntityLookTarget {
    public static BehaviorControl<LivingEntity> create(MobCategory $$0, float $$12) {
        return SetEntityLookTarget.create((LivingEntity $$1) -> $$0.equals($$1.getType().getCategory()), $$12);
    }

    public static OneShot<LivingEntity> create(EntityType<?> $$0, float $$12) {
        return SetEntityLookTarget.create((LivingEntity $$1) -> $$0.equals($$1.getType()), $$12);
    }

    public static OneShot<LivingEntity> create(float $$02) {
        return SetEntityLookTarget.create((LivingEntity $$0) -> true, $$02);
    }

    public static OneShot<LivingEntity> create(Predicate<LivingEntity> $$0, float $$1) {
        float $$22 = $$1 * $$1;
        return BehaviorBuilder.create($$2 -> $$2.group($$2.absent(MemoryModuleType.LOOK_TARGET), $$2.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)$$2, ($$3, $$4) -> ($$5, $$6, $$7) -> {
            Optional<LivingEntity> $$8 = ((NearestVisibleLivingEntities)$$2.get($$4)).findClosest($$0.and($$2 -> $$2.distanceToSqr($$6) <= (double)$$22 && !$$6.hasPassenger((Entity)$$2)));
            if ($$8.isEmpty()) {
                return false;
            }
            $$3.set(new EntityTracker($$8.get(), true));
            return true;
        }));
    }
}

