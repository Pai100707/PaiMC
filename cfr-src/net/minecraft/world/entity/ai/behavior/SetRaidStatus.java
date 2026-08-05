/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class SetRaidStatus {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create($$02 -> $$02.point(($$0, $$1, $$2) -> {
            if ($$0.random.nextInt(20) != 0) {
                return false;
            }
            Brain<?> $$3 = $$1.getBrain();
            Raid $$4 = $$0.getRaidAt($$1.blockPosition());
            if ($$4 != null) {
                if (!$$4.hasFirstWaveSpawned() || $$4.isBetweenWaves()) {
                    $$3.setDefaultActivity(Activity.PRE_RAID);
                    $$3.setActiveActivityIfPossible(Activity.PRE_RAID);
                } else {
                    $$3.setDefaultActivity(Activity.RAID);
                    $$3.setActiveActivityIfPossible(Activity.RAID);
                }
            }
            return true;
        }));
    }
}

