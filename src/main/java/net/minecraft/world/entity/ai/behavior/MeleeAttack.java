package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class MeleeAttack {
   public static <T extends net.minecraft.world.entity.Mob> OneShot<T> create(int $$0) {
      return create($$0x -> true, $$0);
   }

   public static <T extends net.minecraft.world.entity.Mob> OneShot<T> create(Predicate<T> $$0, int $$1) {
      return BehaviorBuilder.create(
         $$2 -> $$2.group(
               $$2.registered(MemoryModuleType.LOOK_TARGET),
               $$2.present(MemoryModuleType.ATTACK_TARGET),
               $$2.absent(MemoryModuleType.ATTACK_COOLING_DOWN),
               $$2.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            )
            .apply(
               $$2,
               ($$3, $$4, $$5, $$6) -> ($$7, $$8, $$9) -> {
                  net.minecraft.world.entity.LivingEntity $$10 = $$2.get($$4);
                  if ($$0.test((T)$$8)
                     && !isHoldingUsableNonMeleeWeapon($$8)
                     && $$8.isWithinMeleeAttackRange($$10)
                     && $$2.<NearestVisibleLivingEntities>get($$6).contains($$10)) {
                     $$3.set(new EntityTracker($$10, true));
                     $$8.swing(InteractionHand.MAIN_HAND);
                     $$8.doHurtTarget($$7, $$10);
                     $$5.setWithExpiry(true, $$1);
                     return true;
                  } else {
                     return false;
                  }
               }
            )
      );
   }

   private static boolean isHoldingUsableNonMeleeWeapon(net.minecraft.world.entity.Mob $$0) {
      return $$0.isHolding($$0::canUseNonMeleeWeapon);
   }
}
