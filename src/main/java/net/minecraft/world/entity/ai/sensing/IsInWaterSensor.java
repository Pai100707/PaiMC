package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class IsInWaterSensor extends Sensor<net.minecraft.world.entity.LivingEntity> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.IS_IN_WATER);
   }

   @Override
   protected void doTick(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      if ($$1.isInWater()) {
         $$1.getBrain().setMemory(MemoryModuleType.IS_IN_WATER, Unit.INSTANCE);
      } else {
         $$1.getBrain().eraseMemory(MemoryModuleType.IS_IN_WATER);
      }
   }
}
