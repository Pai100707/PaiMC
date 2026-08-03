package net.minecraft.world.entity.monster.piglin;

import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class StartAdmiringItemIfSeen {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(int $$0) {
      return BehaviorBuilder.create(
         $$1 -> $$1.group(
               $$1.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
               $$1.absent(MemoryModuleType.ADMIRING_ITEM),
               $$1.absent(MemoryModuleType.ADMIRING_DISABLED),
               $$1.absent(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)
            )
            .apply($$1, ($$2, $$3, $$4, $$5) -> ($$4x, $$5x, $$6) -> {
               ItemEntity $$7 = $$1.get($$2);
               if (!PiglinAi.isLovedItem($$7.getItem())) {
                  return false;
               } else {
                  $$3.setWithExpiry(true, $$0);
                  return true;
               }
            })
      );
   }
}
