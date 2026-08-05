package net.minecraft.world.entity.ai.behavior;

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
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableLong;

public class AcquirePoi {
   public static final int SCAN_RANGE = 48;

   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> create(
      Predicate<Holder<PoiType>> $$0, MemoryModuleType<GlobalPos> $$1, boolean $$2, Optional<Byte> $$3, BiPredicate<ServerLevel, BlockPos> $$4
   ) {
      return create($$0, $$1, $$1, $$2, $$3, $$4);
   }

   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> create(
      Predicate<Holder<PoiType>> $$0, MemoryModuleType<GlobalPos> $$1, boolean $$2, Optional<Byte> $$3
   ) {
      return create($$0, $$1, $$1, $$2, $$3, ($$0x, $$1x) -> true);
   }

   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> create(
      Predicate<Holder<PoiType>> $$0,
      MemoryModuleType<GlobalPos> $$1,
      MemoryModuleType<GlobalPos> $$2,
      boolean $$3,
      Optional<Byte> $$4,
      BiPredicate<ServerLevel, BlockPos> $$5
   ) {
      int $$6 = 5;
      int $$7 = 20;
      MutableLong $$8 = new MutableLong(0L);
      Long2ObjectMap<AcquirePoi.JitteredLinearRetry> $$9 = new Long2ObjectOpenHashMap();
      OneShot<net.minecraft.world.entity.PathfinderMob> $$10 = BehaviorBuilder.create(
         $$7x -> $$7x.group($$7x.absent($$2))
            .apply(
               $$7x,
               $$6xx -> ($$7xx, $$8x, $$9x) -> {
                  if ($$3 && $$8x.isBaby()) {
                     return false;
                  } else if ($$8.longValue() == 0L) {
                     $$8.setValue($$7xx.getGameTime() + $$7xx.random.nextInt(20));
                     return false;
                  } else if ($$7xx.getGameTime() < $$8.longValue()) {
                     return false;
                  } else {
                     $$8.setValue($$9x + 20L + $$7xx.getRandom().nextInt(20));
                     PoiManager $$10x = $$7xx.getPoiManager();
                     $$9.long2ObjectEntrySet().removeIf($$1xxxx -> !((AcquirePoi.JitteredLinearRetry)$$1xxxx.getValue()).isStillValid($$9x));
                     Predicate<BlockPos> $$11 = $$2xxxx -> {
                        AcquirePoi.JitteredLinearRetry $$3xxxx = (AcquirePoi.JitteredLinearRetry)$$9.get($$2xxxx.asLong());
                        if ($$3xxxx == null) {
                           return true;
                        } else if (!$$3xxxx.shouldRetry($$9x)) {
                           return false;
                        } else {
                           $$3xxxx.markAttempt($$9x);
                           return true;
                        }
                     };
                     Set<Pair<Holder<PoiType>, BlockPos>> $$12 = $$10x.findAllClosestFirstWithType(
                           $$0, $$11, $$8x.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE
                        )
                        .limit(5L)
                        .filter($$2xxxx -> $$5.test($$7xx, (BlockPos)$$2xxxx.getSecond()))
                        .collect(Collectors.toSet());
                     Path $$13 = findPathToPois($$8x, $$12);
                     if ($$13 != null && $$13.canReach()) {
                        BlockPos $$14 = $$13.getTarget();
                        $$10x.getType($$14).ifPresent($$8xx -> {
                           $$10x.take($$0, ($$1xxxxx, $$2xxxxx) -> $$2xxxxx.equals($$14), $$14, 1);
                           $$6xx.set(GlobalPos.of($$7xx.dimension(), $$14));
                           $$4.ifPresent($$2xxxxx -> $$7xx.broadcastEntityEvent($$8x, $$2xxxxx));
                           $$9.clear();
                           $$7xx.debugSynchronizers().updatePoi($$14);
                        });
                     } else {
                        for (Pair<Holder<PoiType>, BlockPos> $$15 : $$12) {
                           $$9.computeIfAbsent(((BlockPos)$$15.getSecond()).asLong(), $$2xxxx -> new AcquirePoi.JitteredLinearRetry($$7xx.random, $$9x));
                        }
                     }

                     return true;
                  }
               }
            )
      );
      return $$2 == $$1 ? $$10 : BehaviorBuilder.create($$2x -> $$2x.group($$2x.absent($$1)).apply($$2x, $$1xx -> $$10));
   }

   
   public static Path findPathToPois(net.minecraft.world.entity.Mob $$0, Set<Pair<Holder<PoiType>, BlockPos>> $$1) {
      if ($$1.isEmpty()) {
         return null;
      } else {
         Set<BlockPos> $$2 = new HashSet<>();
         int $$3 = 1;

         for (Pair<Holder<PoiType>, BlockPos> $$4 : $$1) {
            $$3 = Math.max($$3, ((PoiType)((Holder)$$4.getFirst()).value()).validRange());
            $$2.add((BlockPos)$$4.getSecond());
         }

         return $$0.getNavigation().createPath($$2, $$3);
      }
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
         this.nextScheduledAttemptTimestamp = $$0 + this.currentDelay;
      }

      public boolean isStillValid(long $$0) {
         return $$0 - this.previousAttemptTimestamp < 400L;
      }

      public boolean shouldRetry(long $$0) {
         return $$0 >= this.nextScheduledAttemptTimestamp;
      }

      @Override
      public String toString() {
         return "RetryMarker{, previousAttemptAt="
            + this.previousAttemptTimestamp
            + ", nextScheduledAttemptAt="
            + this.nextScheduledAttemptTimestamp
            + ", currentDelay="
            + this.currentDelay
            + "}";
      }
   }
}
