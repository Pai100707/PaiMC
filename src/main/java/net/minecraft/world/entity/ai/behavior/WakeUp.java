package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.schedule.Activity;

public class WakeUp {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create() {
      return BehaviorBuilder.create($$0 -> $$0.point(($$0x, $$1, $$2) -> {
         if (!$$1.getBrain().isActive(Activity.REST) && $$1.isSleeping()) {
            $$1.stopSleeping();
            return true;
         } else {
            return false;
         }
      }));
   }
}
