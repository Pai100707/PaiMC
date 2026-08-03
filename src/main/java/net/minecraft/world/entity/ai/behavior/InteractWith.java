package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith {
   public static <T extends net.minecraft.world.entity.LivingEntity> BehaviorControl<net.minecraft.world.entity.LivingEntity> of(
      net.minecraft.world.entity.EntityType<? extends T> $$0, int $$1, MemoryModuleType<T> $$2, float $$3, int $$4
   ) {
      return of($$0, $$1, $$0x -> true, $$0x -> true, $$2, $$3, $$4);
   }

   public static <E extends net.minecraft.world.entity.LivingEntity, T extends net.minecraft.world.entity.LivingEntity> BehaviorControl<E> of(
      net.minecraft.world.entity.EntityType<? extends T> $$0, int $$1, Predicate<E> $$2, Predicate<T> $$3, MemoryModuleType<T> $$4, float $$5, int $$6
   ) {
      int $$7 = $$1 * $$1;
      Predicate<net.minecraft.world.entity.LivingEntity> $$8 = $$2x -> $$0.equals($$2x.getType()) && $$3.test((T)$$2x);
      return BehaviorBuilder.create(
         $$6x -> $$6x.group(
               $$6x.registered($$4),
               $$6x.registered(MemoryModuleType.LOOK_TARGET),
               $$6x.absent(MemoryModuleType.WALK_TARGET),
               $$6x.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            )
            .apply($$6x, ($$6xx, $$7x, $$8x, $$9) -> ($$10, $$11, $$12) -> {
               NearestVisibleLivingEntities $$13 = $$6x.get($$9);
               if ($$2.test((E)$$11) && $$13.contains($$8)) {
                  Optional<net.minecraft.world.entity.LivingEntity> $$14 = $$13.findClosest($$3xxxx -> $$3xxxx.distanceToSqr($$11) <= $$7 && $$8.test($$3xxxx));
                  $$14.ifPresent($$5xxxx -> {
                     $$6xx.set($$5xxxx);
                     $$7x.set(new EntityTracker($$5xxxx, true));
                     $$8x.set(new WalkTarget(new EntityTracker($$5xxxx, false), $$5, $$6));
                  });
                  return true;
               } else {
                  return false;
               }
            })
      );
   }
}
