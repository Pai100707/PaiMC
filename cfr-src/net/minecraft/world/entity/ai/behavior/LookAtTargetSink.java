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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LookAtTargetSink
extends Behavior<Mob> {
    public LookAtTargetSink(int $$0, int $$1) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)), $$0, $$1);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Mob $$12, long $$2) {
        return $$12.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter($$1 -> $$1.isVisibleBy($$12)).isPresent();
    }

    @Override
    protected void stop(ServerLevel $$0, Mob $$1, long $$2) {
        $$1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void tick(ServerLevel $$0, Mob $$12, long $$2) {
        $$12.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent($$1 -> $$12.getLookControl().setLookAt($$1.currentPosition()));
    }
}

