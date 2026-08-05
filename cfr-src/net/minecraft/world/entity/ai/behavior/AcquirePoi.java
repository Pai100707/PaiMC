/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  org.apache.commons.lang3.mutable.MutableLong
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jspecify.annotations.Nullable;

public class AcquirePoi {
    public static final int SCAN_RANGE = 48;

    public static BehaviorControl<PathfinderMob> create(Predicate<Holder<PoiType>> $$0, MemoryModuleType<GlobalPos> $$1, boolean $$2, Optional<Byte> $$3, BiPredicate<ServerLevel, BlockPos> $$4) {
        return AcquirePoi.create($$0, $$1, $$1, $$2, $$3, $$4);
    }

    public static BehaviorControl<PathfinderMob> create(Predicate<Holder<PoiType>> $$02, MemoryModuleType<GlobalPos> $$12, boolean $$2, Optional<Byte> $$3) {
        return AcquirePoi.create($$02, $$12, $$12, $$2, $$3, ($$0, $$1) -> true);
    }

    public static BehaviorControl<PathfinderMob> create(Predicate<Holder<PoiType>> $$0, MemoryModuleType<GlobalPos> $$1, MemoryModuleType<GlobalPos> $$22, boolean $$3, Optional<Byte> $$4, BiPredicate<ServerLevel, BlockPos> $$5) {
        int $$6 = 5;
        int $$7 = 20;
        MutableLong $$8 = new MutableLong(0L);
        Long2ObjectOpenHashMap $$9 = new Long2ObjectOpenHashMap();
        OneShot<PathfinderMob> $$10 = BehaviorBuilder.create(arg_0 -> AcquirePoi.lambda$create$10($$22, $$3, $$8, (Long2ObjectMap)$$9, $$0, $$5, $$4, arg_0));
        if ($$22 == $$1) {
            return $$10;
        }
        return BehaviorBuilder.create($$2 -> $$2.group($$2.absent($$1)).apply((Applicative)$$2, $$1 -> $$10));
    }

    public static @Nullable Path findPathToPois(Mob $$0, Set<Pair<Holder<PoiType>, BlockPos>> $$1) {
        if ($$1.isEmpty()) {
            return null;
        }
        HashSet<BlockPos> $$2 = new HashSet<BlockPos>();
        int $$3 = 1;
        for (Pair<Holder<PoiType>, BlockPos> $$4 : $$1) {
            $$3 = Math.max($$3, ((PoiType)((Holder)$$4.getFirst()).value()).validRange());
            $$2.add((BlockPos)$$4.getSecond());
        }
        return $$0.getNavigation().createPath($$2, $$3);
    }

    private static /* synthetic */ App lambda$create$10(MemoryModuleType $$0, boolean $$1, MutableLong $$2, Long2ObjectMap $$3, Predicate $$4, BiPredicate $$5, Optional $$62, BehaviorBuilder.Instance $$7) {
        return $$7.group($$7.absent($$0)).apply((Applicative)$$7, $$6 -> ($$7, $$82, $$9) -> {
            if ($$1 && $$82.isBaby()) {
                return false;
            }
            if ($$2.longValue() == 0L) {
                $$2.setValue($$7.getGameTime() + (long)$$7.random.nextInt(20));
                return false;
            }
            if ($$7.getGameTime() < $$2.longValue()) {
                return false;
            }
            $$2.setValue($$9 + 20L + (long)$$7.getRandom().nextInt(20));
            PoiManager $$10 = $$7.getPoiManager();
            $$3.long2ObjectEntrySet().removeIf($$1 -> !((JitteredLinearRetry)$$1.getValue()).isStillValid($$9));
            Predicate<BlockPos> $$11 = $$2 -> {
                JitteredLinearRetry $$3 = (JitteredLinearRetry)$$3.get($$2.asLong());
                if ($$3 == null) {
                    return true;
                }
                if (!$$3.shouldRetry($$9)) {
                    return false;
                }
                $$3.markAttempt($$9);
                return true;
            };
            Set<Pair<Holder<PoiType>, BlockPos>> $$122 = $$10.findAllClosestFirstWithType($$4, $$11, $$82.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE).limit(5L).filter($$2 -> $$5.test($$7, (BlockPos)$$2.getSecond())).collect(Collectors.toSet());
            Path $$13 = AcquirePoi.findPathToPois($$82, $$122);
            if ($$13 != null && $$13.canReach()) {
                BlockPos $$14 = $$13.getTarget();
                $$10.getType($$14).ifPresent($$8 -> {
                    $$10.take($$4, ($$1, $$2) -> $$2.equals($$14), $$14, 1);
                    $$6.set(GlobalPos.of($$7.dimension(), $$14));
                    $$62.ifPresent($$2 -> $$7.broadcastEntityEvent($$82, (byte)$$2));
                    $$3.clear();
                    $$7.debugSynchronizers().updatePoi($$14);
                });
            } else {
                for (Pair<Holder<PoiType>, BlockPos> $$15 : $$122) {
                    $$3.computeIfAbsent(((BlockPos)$$15.getSecond()).asLong(), $$2 -> new JitteredLinearRetry($$0.random, $$9));
                }
            }
            return true;
        });
    }

    static class JitteredLinearRetry {
        private static final int MIN_INTERVAL_INCREASE = 40;
        private static final int MAX_INTERVAL_INCREASE = 80;
        private static final int MAX_RETRY_PATHFINDING_INTERVAL = 400;
        private final RandomSource random;
        private long previousAttemptTimestamp;
        private long nextScheduledAttemptTimestamp;
        private int currentDelay;

        JitteredLinearRetry(RandomSource $$0, long $$1) {
            this.random = $$0;
            this.markAttempt($$1);
        }

        public void markAttempt(long $$0) {
            this.previousAttemptTimestamp = $$0;
            int $$1 = this.currentDelay + this.random.nextInt(40) + 40;
            this.currentDelay = Math.min($$1, 400);
            this.nextScheduledAttemptTimestamp = $$0 + (long)this.currentDelay;
        }

        public boolean isStillValid(long $$0) {
            return $$0 - this.previousAttemptTimestamp < 400L;
        }

        public boolean shouldRetry(long $$0) {
            return $$0 >= this.nextScheduledAttemptTimestamp;
        }

        public String toString() {
            return "RetryMarker{, previousAttemptAt=" + this.previousAttemptTimestamp + ", nextScheduledAttemptAt=" + this.nextScheduledAttemptTimestamp + ", currentDelay=" + this.currentDelay + "}";
        }
    }
}

