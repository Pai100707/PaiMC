package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAttackingIfTargetInvalid {
   private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

   public static <E extends net.minecraft.world.entity.Mob> BehaviorControl<E> create(StopAttackingIfTargetInvalid.TargetErasedCallback<E> $$0) {
      return create(($$0x, $$1) -> false, $$0, true);
   }

   public static <E extends net.minecraft.world.entity.Mob> BehaviorControl<E> create(StopAttackingIfTargetInvalid.StopAttackCondition $$0) {
      return create($$0, ($$0x, $$1, $$2) -> {}, true);
   }

   public static <E extends net.minecraft.world.entity.Mob> BehaviorControl<E> create() {
      return create(($$0, $$1) -> false, ($$0, $$1, $$2) -> {}, true);
   }

   public static <E extends net.minecraft.world.entity.Mob> BehaviorControl<E> create(
      StopAttackingIfTargetInvalid.StopAttackCondition $$0, StopAttackingIfTargetInvalid.TargetErasedCallback<E> $$1, boolean $$2
   ) {
      return BehaviorBuilder.create(
         $$3 -> $$3.group($$3.present(MemoryModuleType.ATTACK_TARGET), $$3.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE))
            .apply(
               $$3,
               ($$4, $$5) -> ($$6, $$7, $$8) -> {
                  net.minecraft.world.entity.LivingEntity $$9 = $$3.get($$4);
                  if ($$7.canAttack($$9)
                     && (!$$2 || !isTiredOfTryingToReachTarget($$7, $$3.tryGet($$5)))
                     && $$9.isAlive()
                     && $$9.level() == $$7.level()
                     && !$$0.test($$6, $$9)) {
                     return true;
                  } else {
                     $$1.accept($$6, (E)$$7, $$9);
                     $$4.erase();
                     return true;
                  }
               }
            )
      );
   }

   private static boolean isTiredOfTryingToReachTarget(net.minecraft.world.entity.LivingEntity $$0, Optional<Long> $$1) {
      return $$1.isPresent() && $$0.level().getGameTime() - $$1.get() > 200L;
   }

   @FunctionalInterface
   public interface StopAttackCondition {
      boolean test(ServerLevel var1, net.minecraft.world.entity.LivingEntity var2);
   }

   @FunctionalInterface
   public interface TargetErasedCallback<E> {
      void accept(ServerLevel var1, E var2, net.minecraft.world.entity.LivingEntity var3);
   }
}
