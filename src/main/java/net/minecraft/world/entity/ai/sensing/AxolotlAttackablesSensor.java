package net.minecraft.world.entity.ai.sensing;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AxolotlAttackablesSensor extends NearestVisibleLivingEntitySensor {
   public static final float TARGET_DETECTION_DISTANCE = 8.0F;

   @Override
   protected boolean isMatchingEntity(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, net.minecraft.world.entity.LivingEntity $$2) {
      return this.isClose($$1, $$2)
         && $$2.isInWater()
         && (this.isHostileTarget($$2) || this.isHuntTarget($$1, $$2))
         && Sensor.isEntityAttackable($$0, $$1, $$2);
   }

   private boolean isHuntTarget(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      return !$$0.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && $$1.getType().is(EntityTypeTags.AXOLOTL_HUNT_TARGETS);
   }

   private boolean isHostileTarget(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getType().is(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES);
   }

   private boolean isClose(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      return $$1.distanceToSqr($$0) <= 64.0;
   }

   @Override
   protected MemoryModuleType<net.minecraft.world.entity.LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}
