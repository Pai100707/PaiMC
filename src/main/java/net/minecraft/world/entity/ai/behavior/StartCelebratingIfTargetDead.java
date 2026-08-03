package net.minecraft.world.entity.ai.behavior;

import java.util.function.BiPredicate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.gamerules.GameRules;

public class StartCelebratingIfTargetDead {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(
      int $$0, BiPredicate<net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.LivingEntity> $$1
   ) {
      return BehaviorBuilder.create(
         $$2 -> $$2.group(
               $$2.present(MemoryModuleType.ATTACK_TARGET),
               $$2.registered(MemoryModuleType.ANGRY_AT),
               $$2.absent(MemoryModuleType.CELEBRATE_LOCATION),
               $$2.registered(MemoryModuleType.DANCING)
            )
            .apply($$2, ($$3, $$4, $$5, $$6) -> ($$7, $$8, $$9) -> {
               net.minecraft.world.entity.LivingEntity $$10 = $$2.get($$3);
               if (!$$10.isDeadOrDying()) {
                  return false;
               } else {
                  if ($$1.test($$8, $$10)) {
                     $$6.setWithExpiry(true, $$0);
                  }

                  $$5.setWithExpiry($$10.blockPosition(), $$0);
                  if ($$10.getType() != net.minecraft.world.entity.EntityType.PLAYER || (Boolean)$$7.getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS)) {
                     $$3.erase();
                     $$4.erase();
                  }

                  return true;
               }
            })
      );
   }
}
