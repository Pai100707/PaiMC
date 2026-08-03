package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class StayCloseToTarget {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(
      Function<net.minecraft.world.entity.LivingEntity, Optional<PositionTracker>> $$0,
      Predicate<net.minecraft.world.entity.LivingEntity> $$1,
      int $$2,
      int $$3,
      float $$4
   ) {
      return BehaviorBuilder.create(
         $$5 -> $$5.group($$5.registered(MemoryModuleType.LOOK_TARGET), $$5.registered(MemoryModuleType.WALK_TARGET))
            .apply($$5, ($$5x, $$6) -> ($$7, $$8, $$9) -> {
               Optional<PositionTracker> $$10 = $$0.apply($$8);
               if (!$$10.isEmpty() && $$1.test($$8)) {
                  PositionTracker $$11 = $$10.get();
                  if ($$8.position().closerThan($$11.currentPosition(), $$3)) {
                     return false;
                  } else {
                     PositionTracker $$12 = $$10.get();
                     $$5x.set($$12);
                     $$6.set(new WalkTarget($$12, $$4, $$2));
                     return true;
                  }
               } else {
                  return false;
               }
            })
      );
   }
}
