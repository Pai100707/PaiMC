/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class MeleeAttack {
    public static <T extends Mob> OneShot<T> create(int $$02) {
        return MeleeAttack.create($$0 -> true, $$02);
    }

    public static <T extends Mob> OneShot<T> create(Predicate<T> $$0, int $$1) {
        return BehaviorBuilder.create($$2 -> $$2.group($$2.registered(MemoryModuleType.LOOK_TARGET), $$2.present(MemoryModuleType.ATTACK_TARGET), $$2.absent(MemoryModuleType.ATTACK_COOLING_DOWN), $$2.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)$$2, ($$3, $$4, $$5, $$6) -> ($$7, $$8, $$9) -> {
            LivingEntity $$10 = (LivingEntity)$$2.get($$4);
            if ($$0.test($$8) && !MeleeAttack.isHoldingUsableNonMeleeWeapon($$8) && $$8.isWithinMeleeAttackRange($$10) && ((NearestVisibleLivingEntities)$$2.get($$6)).contains($$10)) {
                $$3.set(new EntityTracker($$10, true));
                $$8.swing(InteractionHand.MAIN_HAND);
                $$8.doHurtTarget($$7, $$10);
                $$5.setWithExpiry(true, $$1);
                return true;
            }
            return false;
        }));
    }

    private static boolean isHoldingUsableNonMeleeWeapon(Mob $$0) {
        return $$0.isHolding($$0::canUseNonMeleeWeapon);
    }
}

