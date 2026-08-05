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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LongJumpMidJump
extends Behavior<Mob> {
    public static final int TIME_OUT_DURATION = 100;
    private final UniformInt timeBetweenLongJumps;
    private final SoundEvent landingSound;

    public LongJumpMidJump(UniformInt $$0, SoundEvent $$1) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryStatus.VALUE_PRESENT)), 100);
        this.timeBetweenLongJumps = $$0;
        this.landingSound = $$1;
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Mob $$1, long $$2) {
        return !$$1.onGround();
    }

    @Override
    protected void start(ServerLevel $$0, Mob $$1, long $$2) {
        $$1.setDiscardFriction(true);
        $$1.setPose(Pose.LONG_JUMPING);
    }

    @Override
    protected void stop(ServerLevel $$0, Mob $$1, long $$2) {
        if ($$1.onGround()) {
            $$1.setDeltaMovement($$1.getDeltaMovement().multiply(0.1f, 1.0, 0.1f));
            $$0.playSound(null, $$1, this.landingSound, SoundSource.NEUTRAL, 2.0f, 1.0f);
        }
        $$1.setDiscardFriction(false);
        $$1.setPose(Pose.STANDING);
        $$1.getBrain().eraseMemory(MemoryModuleType.LONG_JUMP_MID_JUMP);
        $$1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample($$0.random));
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Mob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Mob)livingEntity, l);
    }
}

