package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.phys.AABB;

public class NearestLivingEntitySensor<T extends net.minecraft.world.entity.LivingEntity> extends Sensor<T> {
   @Override
   protected void doTick(ServerLevel $$0, T $$1) {
      double $$2 = $$1.getAttributeValue(Attributes.FOLLOW_RANGE);
      AABB $$3 = $$1.getBoundingBox().inflate($$2, $$2, $$2);
      List<net.minecraft.world.entity.LivingEntity> $$4 = $$0.getEntitiesOfClass(
         net.minecraft.world.entity.LivingEntity.class, $$3, $$1x -> $$1x != $$1 && $$1x.isAlive()
      );
      $$4.sort(Comparator.comparingDouble($$1::distanceToSqr));
      Brain<?> $$5 = $$1.getBrain();
      $$5.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, $$4);
      $$5.setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new NearestVisibleLivingEntities($$0, $$1, $$4));
   }

   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
   }
}
