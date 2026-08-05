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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class RandomLookAround
extends Behavior<Mob> {
    private final IntProvider interval;
    private final float maxYaw;
    private final float minPitch;
    private final float pitchRange;

    public RandomLookAround(IntProvider $$0, float $$1, float $$2, float $$3) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.GAZE_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT)));
        if ($$2 > $$3) {
            throw new IllegalArgumentException("Minimum pitch is larger than maximum pitch! " + $$2 + " > " + $$3);
        }
        this.interval = $$0;
        this.maxYaw = $$1;
        this.minPitch = $$2;
        this.pitchRange = $$3 - $$2;
    }

    @Override
    protected void start(ServerLevel $$0, Mob $$1, long $$2) {
        RandomSource $$3 = $$1.getRandom();
        float $$4 = Mth.clamp($$3.nextFloat() * this.pitchRange + this.minPitch, -90.0f, 90.0f);
        float $$5 = Mth.wrapDegrees($$1.getYRot() + 2.0f * $$3.nextFloat() * this.maxYaw - this.maxYaw);
        Vec3 $$6 = Vec3.directionFromRotation($$4, $$5);
        $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker($$1.getEyePosition().add($$6)));
        $$1.getBrain().setMemory(MemoryModuleType.GAZE_COOLDOWN_TICKS, this.interval.sample($$3));
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Mob)livingEntity, l);
    }
}

