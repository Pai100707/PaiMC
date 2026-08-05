/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Sniffing<E extends Warden>
extends Behavior<E> {
    private static final double ANGER_FROM_SNIFFING_MAX_DISTANCE_XZ = 6.0;
    private static final double ANGER_FROM_SNIFFING_MAX_DISTANCE_Y = 20.0;

    public Sniffing(int $$0) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.IS_SNIFFING, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.NEAREST_ATTACKABLE, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.DISTURBANCE_LOCATION, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.SNIFF_COOLDOWN, (Object)((Object)MemoryStatus.REGISTERED)), $$0);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
        return true;
    }

    @Override
    protected void start(ServerLevel $$0, E $$1, long $$2) {
        ((Entity)$$1).playSound(SoundEvents.WARDEN_SNIFF, 5.0f, 1.0f);
    }

    @Override
    protected void stop(ServerLevel $$0, E $$12, long $$2) {
        if (((Entity)$$12).hasPose(Pose.SNIFFING)) {
            ((Entity)$$12).setPose(Pose.STANDING);
        }
        ((Warden)$$12).getBrain().eraseMemory(MemoryModuleType.IS_SNIFFING);
        ((Warden)$$12).getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).filter(arg_0 -> $$12.canTargetEntity(arg_0)).ifPresent($$1 -> {
            if ($$12.closerThan((Entity)$$1, 6.0, 20.0)) {
                $$12.increaseAngerAt((Entity)$$1);
            }
            if (!$$12.getBrain().hasMemoryValue(MemoryModuleType.DISTURBANCE_LOCATION)) {
                WardenAi.setDisturbanceLocation($$12, $$1.blockPosition());
            }
        });
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (E)((Warden)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((Warden)livingEntity), l);
    }
}

