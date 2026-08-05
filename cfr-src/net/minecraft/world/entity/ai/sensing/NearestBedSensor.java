/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.longs.Long2LongMap
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;

public class NearestBedSensor
extends Sensor<Mob> {
    private static final int CACHE_TIMEOUT = 40;
    private static final int BATCH_SIZE = 5;
    private static final int RATE = 20;
    private final Long2LongMap batchCache = new Long2LongOpenHashMap();
    private int triedCount;
    private long lastUpdate;

    public NearestBedSensor() {
        super(20);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_BED);
    }

    @Override
    protected void doTick(ServerLevel $$02, Mob $$1) {
        Predicate<BlockPos> $$3;
        if (!$$1.isBaby()) {
            return;
        }
        this.triedCount = 0;
        this.lastUpdate = $$02.getGameTime() + (long)$$02.getRandom().nextInt(20);
        PoiManager $$2 = $$02.getPoiManager();
        Set<Pair<Holder<PoiType>, BlockPos>> $$4 = $$2.findAllWithType($$0 -> $$0.is(PoiTypes.HOME), $$3 = $$0 -> {
            long $$1 = $$0.asLong();
            if (this.batchCache.containsKey($$1)) {
                return false;
            }
            if (++this.triedCount >= 5) {
                return false;
            }
            this.batchCache.put($$1, this.lastUpdate + 40L);
            return true;
        }, $$1.blockPosition(), 48, PoiManager.Occupancy.ANY).collect(Collectors.toSet());
        Path $$5 = AcquirePoi.findPathToPois($$1, $$4);
        if ($$5 != null && $$5.canReach()) {
            BlockPos $$6 = $$5.getTarget();
            Optional<Holder<PoiType>> $$7 = $$2.getType($$6);
            if ($$7.isPresent()) {
                $$1.getBrain().setMemory(MemoryModuleType.NEAREST_BED, $$6);
            }
        } else if (this.triedCount < 5) {
            this.batchCache.long2LongEntrySet().removeIf($$0 -> $$0.getLongValue() < this.lastUpdate);
        }
    }
}

