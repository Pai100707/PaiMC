package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.gamerules.GameRules;

public class StopBeingAngryIfTargetDead {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create() {
      return BehaviorBuilder.create(
         $$0 -> $$0.group($$0.present(MemoryModuleType.ANGRY_AT))
            .apply(
               $$0,
               $$1 -> ($$2, $$3, $$4) -> {
                  Optional.ofNullable($$2.getEntity($$0.get($$1)))
                     .map($$0xxx -> $$0xxx instanceof net.minecraft.world.entity.LivingEntity $$1xx ? $$1xx : null)
                     .filter(net.minecraft.world.entity.LivingEntity::isDeadOrDying)
                     .filter(
                        $$1xx -> $$1xx.getType() != net.minecraft.world.entity.EntityType.PLAYER
                           || (Boolean)$$2.getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS)
                     )
                     .ifPresent($$1xx -> $$1.erase());
                  return true;
               }
            )
      );
   }
}
