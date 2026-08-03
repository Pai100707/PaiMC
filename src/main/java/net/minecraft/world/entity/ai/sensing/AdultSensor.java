package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class AdultSensor extends Sensor<net.minecraft.world.entity.LivingEntity> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
   }

   @Override
   protected void doTick(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      $$1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent($$1x -> this.setNearestVisibleAdult($$1, $$1x));
   }

   protected void setNearestVisibleAdult(net.minecraft.world.entity.LivingEntity $$0, NearestVisibleLivingEntities $$1) {
      Optional<net.minecraft.world.entity.LivingEntity> $$2 = $$1.findClosest($$1x -> $$1x.getType() == $$0.getType() && !$$1x.isBaby());
      $$0.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, $$2);
   }
}
