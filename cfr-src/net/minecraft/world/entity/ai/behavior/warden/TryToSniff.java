/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior.warden;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.util.Unit;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class TryToSniff {
    private static final IntProvider SNIFF_COOLDOWN = UniformInt.of(100, 200);

    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create($$02 -> $$02.group($$02.registered(MemoryModuleType.IS_SNIFFING), $$02.registered(MemoryModuleType.WALK_TARGET), $$02.absent(MemoryModuleType.SNIFF_COOLDOWN), $$02.present(MemoryModuleType.NEAREST_ATTACKABLE), $$02.absent(MemoryModuleType.DISTURBANCE_LOCATION)).apply((Applicative)$$02, ($$0, $$1, $$2, $$32, $$42) -> ($$3, $$4, $$5) -> {
            $$0.set(Unit.INSTANCE);
            $$2.setWithExpiry(Unit.INSTANCE, SNIFF_COOLDOWN.sample($$3.getRandom()));
            $$1.erase();
            $$4.setPose(Pose.SNIFFING);
            return true;
        }));
    }
}

