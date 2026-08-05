/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.longs.Long2LongMap
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.apache.commons.lang3.mutable.MutableLong
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
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
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

public class SetClosestHomeAsWalkTarget {
    private static final int CACHE_TIMEOUT = 40;
    private static final int BATCH_SIZE = 5;
    private static final int RATE = 20;
    private static final int OK_DISTANCE_SQR = 4;

    public static BehaviorControl<PathfinderMob> create(float $$0) {
        Long2LongOpenHashMap $$1 = new Long2LongOpenHashMap();
        MutableLong $$2 = new MutableLong(0L);
        return BehaviorBuilder.create(arg_0 -> SetClosestHomeAsWalkTarget.lambda$create$6($$2, (Long2LongMap)$$1, $$0, arg_0));
    }

    private static /* synthetic */ App lambda$create$6(MutableLong $$0, Long2LongMap $$1, float $$2, BehaviorBuilder.Instance $$32) {
        return $$32.group($$32.absent(MemoryModuleType.WALK_TARGET), $$32.absent(MemoryModuleType.HOME)).apply((Applicative)$$32, ($$3, $$42) -> ($$4, $$5, $$6) -> {
            if ($$4.getGameTime() - $$0.longValue() < 20L) {
                return false;
            }
            PoiManager $$7 = $$4.getPoiManager();
            Optional<BlockPos> $$8 = $$7.findClosest($$0 -> $$0.is(PoiTypes.HOME), $$5.blockPosition(), 48, PoiManager.Occupancy.ANY);
            if ($$8.isEmpty() || $$8.get().distSqr($$5.blockPosition()) <= 4.0) {
                return false;
            }
            MutableInt $$9 = new MutableInt(0);
            $$0.setValue($$4.getGameTime() + (long)$$4.getRandom().nextInt(20));
            Predicate<BlockPos> $$10 = $$3 -> {
                ServerLevel $$4 = $$3.asLong();
                if ($$1.containsKey($$4)) {
                    return false;
                }
                if ($$9.incrementAndGet() >= 5) {
                    return false;
                }
                $$1.put($$4, $$0.longValue() + 40L);
                return true;
            };
            Set<Pair<Holder<PoiType>, BlockPos>> $$11 = $$7.findAllWithType($$0 -> $$0.is(PoiTypes.HOME), $$10, $$5.blockPosition(), 48, PoiManager.Occupancy.ANY).collect(Collectors.toSet());
            Path $$122 = AcquirePoi.findPathToPois($$5, $$11);
            if ($$122 != null && $$122.canReach()) {
                BlockPos $$13 = $$122.getTarget();
                Optional<Holder<PoiType>> $$14 = $$7.getType($$13);
                if ($$14.isPresent()) {
                    $$3.set(new WalkTarget($$13, $$2, 1));
                    $$4.debugSynchronizers().updatePoi($$13);
                }
            } else if ($$9.intValue() < 5) {
                $$1.long2LongEntrySet().removeIf($$1 -> $$1.getLongValue() < $$0.longValue());
            }
            return true;
        });
    }
}

