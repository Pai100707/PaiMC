package net.minecraft.world.entity.ai.behavior;

import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromLookTarget {
   public static OneShot<net.minecraft.world.entity.LivingEntity> create(float $$0, int $$1) {
      return create($$0x -> true, $$1x -> $$0, $$1);
   }

   public static OneShot<net.minecraft.world.entity.LivingEntity> create(
      Predicate<net.minecraft.world.entity.LivingEntity> $$0, Function<net.minecraft.world.entity.LivingEntity, Float> $$1, int $$2
   ) {
      return BehaviorBuilder.create(
         $$3 -> $$3.group($$3.absent(MemoryModuleType.WALK_TARGET), $$3.present(MemoryModuleType.LOOK_TARGET)).apply($$3, ($$4, $$5) -> ($$6, $$7, $$8) -> {
            if (!$$0.test($$7)) {
               return false;
            } else {
               $$4.set(new WalkTarget($$3.get($$5), $$1.apply($$7), $$2));
               return true;
            }
         })
      );
   }
}
