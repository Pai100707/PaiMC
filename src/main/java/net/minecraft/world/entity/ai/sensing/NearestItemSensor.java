package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class NearestItemSensor extends Sensor<net.minecraft.world.entity.Mob> {
   private static final long XZ_RANGE = 32L;
   private static final long Y_RANGE = 16L;
   public static final int MAX_DISTANCE_TO_WANTED_ITEM = 32;

   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
   }

   protected void doTick(ServerLevel $$0, net.minecraft.world.entity.Mob $$1) {
      Brain<?> $$2 = $$1.getBrain();
      List<ItemEntity> $$3 = $$0.getEntitiesOfClass(ItemEntity.class, $$1.getBoundingBox().inflate(32.0, 16.0, 32.0), $$0x -> true);
      $$3.sort(Comparator.comparingDouble($$1::distanceToSqr));
      Optional<ItemEntity> $$4 = $$3.stream()
         .filter($$2x -> $$1.wantsToPickUp($$0, $$2x.getItem()))
         .filter($$1x -> $$1x.closerThan($$1, 32.0))
         .filter($$1::hasLineOfSight)
         .findFirst();
      $$2.setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, $$4);
   }
}
