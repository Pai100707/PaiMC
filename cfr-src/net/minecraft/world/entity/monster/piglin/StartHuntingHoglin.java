/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.monster.piglin;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;

public class StartHuntingHoglin {
    public static OneShot<Piglin> create() {
        return BehaviorBuilder.create($$0 -> $$0.group($$0.present(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN), $$0.absent(MemoryModuleType.ANGRY_AT), $$0.absent(MemoryModuleType.HUNTED_RECENTLY), $$0.registered(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS)).apply((Applicative)$$0, ($$1, $$2, $$32, $$42) -> ($$3, $$4, $$5) -> {
            if ($$4.isBaby() || $$0.tryGet($$42).map($$0 -> $$0.stream().anyMatch(StartHuntingHoglin::hasHuntedRecently)).isPresent()) {
                return false;
            }
            Hoglin $$6 = (Hoglin)$$0.get($$1);
            PiglinAi.setAngerTarget($$3, $$4, $$6);
            PiglinAi.dontKillAnyMoreHoglinsForAWhile($$4);
            PiglinAi.broadcastAngerTarget($$3, $$4, $$6);
            $$0.tryGet($$42).ifPresent($$0 -> $$0.forEach(PiglinAi::dontKillAnyMoreHoglinsForAWhile));
            return true;
        }));
    }

    private static boolean hasHuntedRecently(AbstractPiglin $$0) {
        return $$0.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY);
    }
}

