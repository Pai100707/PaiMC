/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.gamerules.GameRules;

public class StopBeingAngryIfTargetDead {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create($$0 -> $$0.group($$0.present(MemoryModuleType.ANGRY_AT)).apply((Applicative)$$0, $$1 -> ($$2, $$3, $$4) -> {
            Optional.ofNullable($$2.getEntity((UUID)$$0.get($$1))).map($$0 -> {
                LivingEntity $$1;
                return $$0 instanceof LivingEntity ? ($$1 = (LivingEntity)$$0) : null;
            }).filter(LivingEntity::isDeadOrDying).filter($$1 -> $$1.getType() != EntityType.PLAYER || $$2.getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS) != false).ifPresent($$1 -> $$1.erase());
            return true;
        }));
    }
}

