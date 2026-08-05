/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class VillagerPanicTrigger
extends Behavior<Villager> {
    public VillagerPanicTrigger() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of());
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
        return VillagerPanicTrigger.isHurt($$1) || VillagerPanicTrigger.hasHostile($$1);
    }

    @Override
    protected void start(ServerLevel $$0, Villager $$1, long $$2) {
        if (VillagerPanicTrigger.isHurt($$1) || VillagerPanicTrigger.hasHostile($$1)) {
            Brain<Villager> $$3 = $$1.getBrain();
            if (!$$3.isActive(Activity.PANIC)) {
                $$3.eraseMemory(MemoryModuleType.PATH);
                $$3.eraseMemory(MemoryModuleType.WALK_TARGET);
                $$3.eraseMemory(MemoryModuleType.LOOK_TARGET);
                $$3.eraseMemory(MemoryModuleType.BREED_TARGET);
                $$3.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
            }
            $$3.setActiveActivityIfPossible(Activity.PANIC);
        }
    }

    @Override
    protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
        if ($$2 % 100L == 0L) {
            $$1.spawnGolemIfNeeded($$0, $$2, 3);
        }
    }

    public static boolean hasHostile(LivingEntity $$0) {
        return $$0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE);
    }

    public static boolean isHurt(LivingEntity $$0) {
        return $$0.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

