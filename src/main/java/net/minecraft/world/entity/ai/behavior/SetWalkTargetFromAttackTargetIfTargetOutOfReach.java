package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach {
   private static final int PROJECTILE_ATTACK_RANGE_BUFFER = 1;

   public static BehaviorControl<net.minecraft.world.entity.Mob> create(float $$0) {
      return create($$1 -> $$0);
   }

   public static BehaviorControl<net.minecraft.world.entity.Mob> create(Function<net.minecraft.world.entity.LivingEntity, Float> $$0) {
      return BehaviorBuilder.create(
         $$1 -> $$1.group(
               $$1.registered(MemoryModuleType.WALK_TARGET),
               $$1.registered(MemoryModuleType.LOOK_TARGET),
               $$1.present(MemoryModuleType.ATTACK_TARGET),
               $$1.registered(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            )
            .apply($$1, ($$2, $$3, $$4, $$5) -> ($$6, $$7, $$8) -> {
               net.minecraft.world.entity.LivingEntity $$9 = $$1.get($$4);
               Optional<NearestVisibleLivingEntities> $$10 = $$1.tryGet($$5);
               if ($$10.isPresent() && $$10.get().contains($$9) && BehaviorUtils.isWithinAttackRange($$7, $$9, 1)) {
                  $$2.erase();
               } else {
                  $$3.set(new EntityTracker($$9, true));
                  $$2.set(new WalkTarget(new EntityTracker($$9, false), $$0.apply($$7), 0));
               }

               return true;
            })
      );
   }
}
