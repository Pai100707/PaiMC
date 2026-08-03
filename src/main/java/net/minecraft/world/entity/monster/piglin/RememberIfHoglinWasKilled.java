package net.minecraft.world.entity.monster.piglin;

import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class RememberIfHoglinWasKilled {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create() {
      return BehaviorBuilder.create(
         $$0 -> $$0.group($$0.present(MemoryModuleType.ATTACK_TARGET), $$0.registered(MemoryModuleType.HUNTED_RECENTLY))
            .apply($$0, ($$1, $$2) -> ($$3, $$4, $$5) -> {
               net.minecraft.world.entity.LivingEntity $$6 = $$0.get($$1);
               if ($$6.getType() == net.minecraft.world.entity.EntityType.HOGLIN && $$6.isDeadOrDying()) {
                  $$2.setWithExpiry(true, PiglinAi.TIME_BETWEEN_HUNTS.sample($$4.level().random));
               }

               return true;
            })
      );
   }
}
