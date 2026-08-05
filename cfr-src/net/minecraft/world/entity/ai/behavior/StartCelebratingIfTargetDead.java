/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.BiPredicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.gamerules.GameRules;

public class StartCelebratingIfTargetDead {
    public static BehaviorControl<LivingEntity> create(int $$0, BiPredicate<LivingEntity, LivingEntity> $$1) {
        return BehaviorBuilder.create($$2 -> $$2.group($$2.present(MemoryModuleType.ATTACK_TARGET), $$2.registered(MemoryModuleType.ANGRY_AT), $$2.absent(MemoryModuleType.CELEBRATE_LOCATION), $$2.registered(MemoryModuleType.DANCING)).apply((Applicative)$$2, ($$3, $$4, $$5, $$6) -> ($$7, $$8, $$9) -> {
            LivingEntity $$10 = (LivingEntity)$$2.get($$3);
            if (!$$10.isDeadOrDying()) {
                return false;
            }
            if ($$1.test($$8, $$10)) {
                $$6.setWithExpiry(true, $$0);
            }
            $$5.setWithExpiry($$10.blockPosition(), $$0);
            if ($$10.getType() != EntityType.PLAYER || $$7.getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS).booleanValue()) {
                $$3.erase();
                $$4.erase();
            }
            return true;
        }));
    }
}

