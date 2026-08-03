package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetEntityLookTarget {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(net.minecraft.world.entity.MobCategory $$0, float $$1) {
      return create($$1x -> $$0.equals($$1x.getType().getCategory()), $$1);
   }

   public static OneShot<net.minecraft.world.entity.LivingEntity> create(net.minecraft.world.entity.EntityType<?> $$0, float $$1) {
      return create($$1x -> $$0.equals($$1x.getType()), $$1);
   }

   public static OneShot<net.minecraft.world.entity.LivingEntity> create(float $$0) {
      return create($$0x -> true, $$0);
   }

   public static OneShot<net.minecraft.world.entity.LivingEntity> create(Predicate<net.minecraft.world.entity.LivingEntity> $$0, float $$1) {
      float $$2 = $$1 * $$1;
      return BehaviorBuilder.create(
         $$2x -> $$2x.group($$2x.absent(MemoryModuleType.LOOK_TARGET), $$2x.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
            .apply(
               $$2x,
               ($$3, $$4) -> ($$5, $$6, $$7) -> {
                  Optional<net.minecraft.world.entity.LivingEntity> $$8 = $$2x.<NearestVisibleLivingEntities>get($$4)
                     .findClosest($$0.and($$2xxxx -> $$2xxxx.distanceToSqr($$6) <= $$2 && !$$6.hasPassenger($$2xxxx)));
                  if ($$8.isEmpty()) {
                     return false;
                  } else {
                     $$3.set(new EntityTracker($$8.get(), true));
                     return true;
                  }
               }
            )
      );
   }
}
