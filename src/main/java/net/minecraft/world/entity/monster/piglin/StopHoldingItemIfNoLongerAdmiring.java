package net.minecraft.world.entity.monster.piglin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopHoldingItemIfNoLongerAdmiring {
   public static BehaviorControl<Piglin> create() {
      return BehaviorBuilder.create($$0 -> $$0.group($$0.absent(MemoryModuleType.ADMIRING_ITEM)).apply($$0, $$0x -> ($$0xx, $$1, $$2) -> {
         if (!$$1.getOffhandItem().isEmpty() && !$$1.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS)) {
            PiglinAi.stopHoldingOffHandItem($$0xx, $$1, true);
            return true;
         } else {
            return false;
         }
      }));
   }
}
