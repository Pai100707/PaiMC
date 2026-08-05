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
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith {
    public static <T extends LivingEntity> BehaviorControl<LivingEntity> of(EntityType<? extends T> $$02, int $$1, MemoryModuleType<T> $$2, float $$3, int $$4) {
        return InteractWith.of($$02, $$1, $$0 -> true, $$0 -> true, $$2, $$3, $$4);
    }

    public static <E extends LivingEntity, T extends LivingEntity> BehaviorControl<E> of(EntityType<? extends T> $$0, int $$1, Predicate<E> $$22, Predicate<T> $$3, MemoryModuleType<T> $$4, float $$5, int $$6) {
        int $$7 = $$1 * $$1;
        Predicate<LivingEntity> $$8 = $$2 -> $$0.equals($$2.getType()) && $$3.test($$2);
        return BehaviorBuilder.create($$62 -> $$62.group($$62.registered($$4), $$62.registered(MemoryModuleType.LOOK_TARGET), $$62.absent(MemoryModuleType.WALK_TARGET), $$62.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)$$62, ($$6, $$7, $$8, $$9) -> ($$10, $$11, $$12) -> {
            NearestVisibleLivingEntities $$13 = (NearestVisibleLivingEntities)$$62.get($$9);
            if ($$22.test($$11) && $$13.contains($$8)) {
                Optional<LivingEntity> $$14 = $$13.findClosest($$3 -> $$3.distanceToSqr($$11) <= (double)$$7 && $$8.test((LivingEntity)$$3));
                $$14.ifPresent($$5 -> {
                    $$6.set($$5);
                    $$7.set(new EntityTracker((Entity)$$5, true));
                    $$8.set(new WalkTarget(new EntityTracker((Entity)$$5, false), $$5, $$6));
                });
                return true;
            }
            return false;
        }));
    }
}

