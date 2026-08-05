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

public class Digging<E extends Warden>
extends Behavior<E> {
    public Digging(int $$0) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), $$0);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
        return ((Entity)$$1).getRemovalReason() == null;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, E $$1) {
        return ((Entity)$$1).onGround() || ((Entity)$$1).isInWater() || ((Entity)$$1).isInLava();
    }

    @Override
    protected void start(ServerLevel $$0, E $$1, long $$2) {
        if (((Entity)$$1).onGround()) {
            ((Entity)$$1).setPose(Pose.DIGGING);
            ((Entity)$$1).playSound(SoundEvents.WARDEN_DIG, 5.0f, 1.0f);
        } else {
            ((Entity)$$1).playSound(SoundEvents.WARDEN_AGITATED, 5.0f, 1.0f);
            this.stop($$0, $$1, $$2);
        }
    }

    @Override
    protected void stop(ServerLevel $$0, E $$1, long $$2) {
        if (((Entity)$$1).getRemovalReason() == null) {
            ((LivingEntity)$$1).remove(Entity.RemovalReason.DISCARDED);
        }
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

