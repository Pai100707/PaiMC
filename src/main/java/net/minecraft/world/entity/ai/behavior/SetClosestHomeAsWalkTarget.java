package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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

   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> create(float $$0) {
      Long2LongMap $$1 = new Long2LongOpenHashMap();
      MutableLong $$2 = new MutableLong(0L);
      return BehaviorBuilder.create(
         $$3 -> $$3.group($$3.absent(MemoryModuleType.WALK_TARGET), $$3.absent(MemoryModuleType.HOME))
            .apply(
               $$3,
               ($$3x, $$4) -> ($$4x, $$5, $$6) -> {
                  if ($$4x.getGameTime() - $$2.longValue() < 20L) {
                     return false;
                  } else {
                     PoiManager $$7 = $$4x.getPoiManager();
                     Optional<BlockPos> $$8 = $$7.findClosest($$0xxxx -> $$0xxxx.is(PoiTypes.HOME), $$5.blockPosition(), 48, PoiManager.Occupancy.ANY);
                     if (!$$8.isEmpty() && !($$8.get().distSqr($$5.blockPosition()) <= 4.0)) {
                        MutableInt $$9 = new MutableInt(0);
                        $$2.setValue($$4x.getGameTime() + $$4x.getRandom().nextInt(20));
                        Predicate<BlockPos> $$10 = $$3xxx -> {
                           long $$4xx = $$3xxx.asLong();
                           if ($$1.containsKey($$4xx)) {
                              return false;
                           } else if ($$9.incrementAndGet() >= 5) {
                              return false;
                           } else {
                              $$1.put($$4xx, $$2.longValue() + 40L);
                              return true;
                           }
                        };
                        Set<Pair<Holder<PoiType>, BlockPos>> $$11 = $$7.findAllWithType(
                              $$0xxxx -> $$0xxxx.is(PoiTypes.HOME), $$10, $$5.blockPosition(), 48, PoiManager.Occupancy.ANY
                           )
                           .collect(Collectors.toSet());
                        Path $$12 = AcquirePoi.findPathToPois($$5, $$11);
                        if ($$12 != null && $$12.canReach()) {
                           BlockPos $$13 = $$12.getTarget();
                           Optional<Holder<PoiType>> $$14 = $$7.getType($$13);
                           if ($$14.isPresent()) {
                              $$3x.set(new WalkTarget($$13, $$0, 1));
                              $$4x.debugSynchronizers().updatePoi($$13);
                           }
                        } else if ($$9.intValue() < 5) {
                           $$1.long2LongEntrySet().removeIf($$1xxxx -> $$1xxxx.getLongValue() < $$2.longValue());
                        }

                        return true;
                     } else {
                        return false;
                     }
                  }
               }
            )
      );
   }
}
