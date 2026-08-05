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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach {
    private static final int PROJECTILE_ATTACK_RANGE_BUFFER = 1;

    public static BehaviorControl<Mob> create(float $$0) {
        return SetWalkTargetFromAttackTargetIfTargetOutOfReach.create($$1 -> Float.valueOf($$0));
    }

    public static BehaviorControl<Mob> create(Function<LivingEntity, Float> $$0) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> $$1) -> $$1.group($$1.registered(MemoryModuleType.WALK_TARGET), $$1.registered(MemoryModuleType.LOOK_TARGET), $$1.present(MemoryModuleType.ATTACK_TARGET), $$1.registered(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)$$1, ($$2, $$3, $$4, $$5) -> ($$6, $$7, $$8) -> {
            LivingEntity $$9 = (LivingEntity)$$1.get($$4);
            Optional $$10 = $$1.tryGet($$5);
            if ($$10.isPresent() && ((NearestVisibleLivingEntities)$$10.get()).contains($$9) && BehaviorUtils.isWithinAttackRange($$7, $$9, 1)) {
                $$2.erase();
            } else {
                $$3.set(new EntityTracker($$9, true));
                $$2.set(new WalkTarget(new EntityTracker($$9, false), ((Float)$$0.apply($$7)).floatValue(), 0));
            }
            return true;
        }));
    }
}

