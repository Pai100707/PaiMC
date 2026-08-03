package net.minecraft.world.entity.ai.behavior.warden;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class SetWardenLookTarget {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create() {
      return BehaviorBuilder.create(
         $$0 -> $$0.group(
               $$0.registered(MemoryModuleType.LOOK_TARGET),
               $$0.registered(MemoryModuleType.DISTURBANCE_LOCATION),
               $$0.registered(MemoryModuleType.ROAR_TARGET),
               $$0.absent(MemoryModuleType.ATTACK_TARGET)
            )
            .apply(
               $$0,
               ($$1, $$2, $$3, $$4) -> ($$4x, $$5, $$6) -> {
                  Optional<BlockPos> $$7 = $$0.<net.minecraft.world.entity.LivingEntity>tryGet($$3)
                     .map(net.minecraft.world.entity.Entity::blockPosition)
                     .or(() -> $$0.tryGet($$2));
                  if ($$7.isEmpty()) {
                     return false;
                  } else {
                     $$1.set(new BlockPosTracker($$7.get()));
                     return true;
                  }
               }
            )
      );
   }
}
