/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SocializeAtBell {
    private static final float SPEED_MODIFIER = 0.3f;

    public static OneShot<LivingEntity> create() {
        return BehaviorBuilder.create($$0 -> $$0.group($$0.registered(MemoryModuleType.WALK_TARGET), $$0.registered(MemoryModuleType.LOOK_TARGET), $$0.present(MemoryModuleType.MEETING_POINT), $$0.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES), $$0.absent(MemoryModuleType.INTERACTION_TARGET)).apply((Applicative)$$0, ($$1, $$2, $$3, $$4, $$5) -> ($$6, $$7, $$8) -> {
            GlobalPos $$9 = (GlobalPos)$$0.get($$3);
            NearestVisibleLivingEntities $$10 = (NearestVisibleLivingEntities)$$0.get($$4);
            if ($$6.getRandom().nextInt(100) == 0 && $$6.dimension() == $$9.dimension() && $$9.pos().closerToCenterThan($$7.position(), 4.0) && $$10.contains($$0 -> EntityType.VILLAGER.equals($$0.getType()))) {
                $$10.findClosest($$1 -> EntityType.VILLAGER.equals($$1.getType()) && $$1.distanceToSqr($$7) <= 32.0).ifPresent($$3 -> {
                    $$5.set($$3);
                    $$2.set(new EntityTracker((Entity)$$3, true));
                    $$1.set(new WalkTarget(new EntityTracker((Entity)$$3, false), 0.3f, 1));
                });
                return true;
            }
            return false;
        }));
    }
}

