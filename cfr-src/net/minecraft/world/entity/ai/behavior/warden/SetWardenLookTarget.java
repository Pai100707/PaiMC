/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior.warden;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class SetWardenLookTarget {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create($$0 -> $$0.group($$0.registered(MemoryModuleType.LOOK_TARGET), $$0.registered(MemoryModuleType.DISTURBANCE_LOCATION), $$0.registered(MemoryModuleType.ROAR_TARGET), $$0.absent(MemoryModuleType.ATTACK_TARGET)).apply((Applicative)$$0, ($$1, $$2, $$3, $$42) -> ($$4, $$5, $$6) -> {
            Optional<BlockPos> $$7 = $$0.tryGet($$3).map(Entity::blockPosition).or(() -> $$0.tryGet($$2));
            if ($$7.isEmpty()) {
                return false;
            }
            $$1.set(new BlockPosTracker($$7.get()));
            return true;
        }));
    }
}

