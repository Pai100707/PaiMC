package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BecomePassiveIfMemoryPresent {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(MemoryModuleType<?> $$0, int $$1) {
      return BehaviorBuilder.create(
         $$2 -> $$2.group($$2.registered(MemoryModuleType.ATTACK_TARGET), $$2.absent(MemoryModuleType.PACIFIED), $$2.present($$0))
            .apply($$2, $$2.point(() -> "[BecomePassive if " + $$0 + " present]", ($$1xx, $$2x, $$3) -> ($$3x, $$4, $$5) -> {
               $$2x.setWithExpiry(true, $$1);
               $$1xx.erase();
               return true;
            }))
      );
   }
}
