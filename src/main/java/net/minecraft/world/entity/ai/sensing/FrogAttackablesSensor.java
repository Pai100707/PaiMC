package net.minecraft.world.entity.ai.sensing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.frog.Frog;

public class FrogAttackablesSensor extends NearestVisibleLivingEntitySensor {
   public static final float TARGET_DETECTION_DISTANCE = 10.0F;

   @Override
   protected boolean isMatchingEntity(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, net.minecraft.world.entity.LivingEntity $$2) {
      return !$$1.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)
            && Sensor.isEntityAttackable($$0, $$1, $$2)
            && Frog.canEat($$2)
            && !this.isUnreachableAttackTarget($$1, $$2)
         ? $$2.closerThan($$1, 10.0)
         : false;
   }

   private boolean isUnreachableAttackTarget(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      List<UUID> $$2 = $$0.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
      return $$2.contains($$1.getUUID());
   }

   @Override
   protected MemoryModuleType<net.minecraft.world.entity.LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}
