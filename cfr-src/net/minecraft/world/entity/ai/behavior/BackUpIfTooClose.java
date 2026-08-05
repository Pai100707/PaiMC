/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class BackUpIfTooClose {
    public static OneShot<Mob> create(int $$0, float $$1) {
        return BehaviorBuilder.create($$2 -> $$2.group($$2.absent(MemoryModuleType.WALK_TARGET), $$2.registered(MemoryModuleType.LOOK_TARGET), $$2.present(MemoryModuleType.ATTACK_TARGET), $$2.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)$$2, ($$3, $$4, $$5, $$62) -> ($$6, $$7, $$8) -> {
            LivingEntity $$9 = (LivingEntity)$$2.get($$5);
            if ($$9.closerThan($$7, $$0) && ((NearestVisibleLivingEntities)$$2.get($$62)).contains($$9)) {
                $$4.set(new EntityTracker($$9, true));
                $$7.getMoveControl().strafe(-$$1, 0.0f);
                $$7.setYRot(Mth.rotateIfNecessary($$7.getYRot(), $$7.yHeadRot, 0.0f));
                return true;
            }
            return false;
        }));
    }
}

