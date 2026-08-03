package net.minecraft.world.entity.ai.sensing;

import java.util.Optional;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class AdultSensorAnyType extends AdultSensor {
   @Override
   protected void setNearestVisibleAdult(net.minecraft.world.entity.LivingEntity $$0, NearestVisibleLivingEntities $$1) {
      Optional<net.minecraft.world.entity.LivingEntity> $$2 = $$1.findClosest(
         $$0x -> $$0x.getType().is(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS) && !$$0x.isBaby()
      );
      $$0.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, $$2);
   }
}
