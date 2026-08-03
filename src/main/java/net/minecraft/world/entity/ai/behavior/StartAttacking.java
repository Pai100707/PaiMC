package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StartAttacking {
   public static <E extends net.minecraft.world.entity.Mob> BehaviorControl<E> create(StartAttacking.TargetFinder<E> $$0) {
      return create(($$0x, $$1) -> true, $$0);
   }

   public static <E extends net.minecraft.world.entity.Mob> BehaviorControl<E> create(
      StartAttacking.StartAttackingCondition<E> $$0, StartAttacking.TargetFinder<E> $$1
   ) {
      return BehaviorBuilder.create(
         $$2 -> $$2.group($$2.absent(MemoryModuleType.ATTACK_TARGET), $$2.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE))
            .apply($$2, ($$2x, $$3) -> ($$4, $$5, $$6) -> {
               if (!$$0.test($$4, (E)$$5)) {
                  return false;
               } else {
                  Optional<? extends net.minecraft.world.entity.LivingEntity> $$7 = $$1.get($$4, (E)$$5);
                  if ($$7.isEmpty()) {
                     return false;
                  } else {
                     net.minecraft.world.entity.LivingEntity $$8 = $$7.get();
                     if (!$$5.canAttack($$8)) {
                        return false;
                     } else {
                        $$2x.set($$8);
                        $$3.erase();
                        return true;
                     }
                  }
               }
            })
      );
   }

   @FunctionalInterface
   public interface StartAttackingCondition<E> {
      boolean test(ServerLevel var1, E var2);
   }

   @FunctionalInterface
   public interface TargetFinder<E> {
      Optional<? extends net.minecraft.world.entity.LivingEntity> get(ServerLevel var1, E var2);
   }
}
