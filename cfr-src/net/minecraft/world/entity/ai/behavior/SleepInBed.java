/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SleepInBed
extends Behavior<LivingEntity> {
    public static final int COOLDOWN_AFTER_BEING_WOKEN = 100;
    private long nextOkStartTime;

    public SleepInBed() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.HOME, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.LAST_WOKEN, (Object)((Object)MemoryStatus.REGISTERED)));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, LivingEntity $$1) {
        long $$5;
        if ($$1.isPassenger()) {
            return false;
        }
        Brain<?> $$2 = $$1.getBrain();
        GlobalPos $$3 = $$2.getMemory(MemoryModuleType.HOME).get();
        if ($$0.dimension() != $$3.dimension()) {
            return false;
        }
        Optional<Long> $$4 = $$2.getMemory(MemoryModuleType.LAST_WOKEN);
        if ($$4.isPresent() && ($$5 = $$0.getGameTime() - $$4.get()) > 0L && $$5 < 100L) {
            return false;
        }
        BlockState $$6 = $$0.getBlockState($$3.pos());
        return $$3.pos().closerToCenterThan($$1.position(), 2.0) && $$6.is(BlockTags.BEDS) && $$6.getValue(BedBlock.OCCUPIED) == false;
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, LivingEntity $$1, long $$2) {
        Optional<GlobalPos> $$3 = $$1.getBrain().getMemory(MemoryModuleType.HOME);
        if ($$3.isEmpty()) {
            return false;
        }
        BlockPos $$4 = $$3.get().pos();
        return $$1.getBrain().isActive(Activity.REST) && $$1.getY() > (double)$$4.getY() + 0.4 && $$4.closerToCenterThan($$1.position(), 1.14);
    }

    @Override
    protected void start(ServerLevel $$0, LivingEntity $$1, long $$2) {
        if ($$2 > this.nextOkStartTime) {
            Brain<Collection<Object>> $$3 = $$1.getBrain();
            if ($$3.hasMemoryValue(MemoryModuleType.DOORS_TO_CLOSE)) {
                Optional<List<LivingEntity>> $$6;
                Set<GlobalPos> $$4 = $$3.getMemory(MemoryModuleType.DOORS_TO_CLOSE).get();
                if ($$3.hasMemoryValue(MemoryModuleType.NEAREST_LIVING_ENTITIES)) {
                    Optional<List<LivingEntity>> $$5 = $$3.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
                } else {
                    $$6 = Optional.empty();
                }
                InteractWithDoor.closeDoorsThatIHaveOpenedOrPassedThrough($$0, $$1, null, null, $$4, $$6);
            }
            $$1.startSleeping($$1.getBrain().getMemory(MemoryModuleType.HOME).get().pos());
        }
    }

    @Override
    protected boolean timedOut(long $$0) {
        return false;
    }

    @Override
    protected void stop(ServerLevel $$0, LivingEntity $$1, long $$2) {
        if ($$1.isSleeping()) {
            $$1.stopSleeping();
            this.nextOkStartTime = $$2 + 40L;
        }
    }
}

