/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster.breeze;

import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.breeze.Breeze;

public class ShootWhenStuck
extends Behavior<Breeze> {
    public ShootWhenStuck() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Breeze $$1) {
        return $$1.isPassenger() || $$1.isInWater() || $$1.getEffect(MobEffects.LEVITATION) != null;
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Breeze $$1, long $$2) {
        return false;
    }

    @Override
    protected void start(ServerLevel $$0, Breeze $$1, long $$2) {
        $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 60L);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Breeze)livingEntity, l);
    }
}

