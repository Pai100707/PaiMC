package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class UpdateActivityFromSchedule {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create() {
      return BehaviorBuilder.create($$0 -> $$0.point(($$0x, $$1, $$2) -> {
         $$1.getBrain().updateActivityFromSchedule($$0x.environmentAttributes(), $$0x.getGameTime(), $$1.position());
         return true;
      }));
   }
}
