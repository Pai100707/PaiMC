/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetLookAndInteract {
    public static BehaviorControl<LivingEntity> create(EntityType<?> $$0, int $$1) {
        int $$22 = $$1 * $$1;
        return BehaviorBuilder.create($$2 -> $$2.group($$2.registered(MemoryModuleType.LOOK_TARGET), $$2.absent(MemoryModuleType.INTERACTION_TARGET), $$2.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)$$2, ($$3, $$4, $$5) -> ($$6, $$7, $$8) -> {
            Optional<LivingEntity> $$9 = ((NearestVisibleLivingEntities)$$2.get($$5)).findClosest($$3 -> $$3.distanceToSqr($$7) <= (double)$$22 && $$0.equals($$3.getType()));
            if ($$9.isEmpty()) {
                return false;
            }
            LivingEntity $$10 = $$9.get();
            $$4.set($$10);
            $$3.set(new EntityTracker($$10, true));
            return true;
        }));
    }
}

