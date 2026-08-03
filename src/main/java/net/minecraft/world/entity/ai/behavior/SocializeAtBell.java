package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SocializeAtBell {
   private static final float SPEED_MODIFIER = 0.3F;

   public static OneShot<net.minecraft.world.entity.LivingEntity> create() {
      return BehaviorBuilder.create(
         $$0 -> $$0.group(
               $$0.registered(MemoryModuleType.WALK_TARGET),
               $$0.registered(MemoryModuleType.LOOK_TARGET),
               $$0.present(MemoryModuleType.MEETING_POINT),
               $$0.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES),
               $$0.absent(MemoryModuleType.INTERACTION_TARGET)
            )
            .apply(
               $$0,
               ($$1, $$2, $$3, $$4, $$5) -> ($$6, $$7, $$8) -> {
                  GlobalPos $$9 = $$0.get($$3);
                  NearestVisibleLivingEntities $$10 = $$0.get($$4);
                  if ($$6.getRandom().nextInt(100) == 0
                     && $$6.dimension() == $$9.dimension()
                     && $$9.pos().closerToCenterThan($$7.position(), 4.0)
                     && $$10.contains($$0xxx -> net.minecraft.world.entity.EntityType.VILLAGER.equals($$0xxx.getType()))) {
                     $$10.findClosest($$1xx -> net.minecraft.world.entity.EntityType.VILLAGER.equals($$1xx.getType()) && $$1xx.distanceToSqr($$7) <= 32.0)
                        .ifPresent($$3xx -> {
                           $$5.set($$3xx);
                           $$2.set(new EntityTracker($$3xx, true));
                           $$1.set(new WalkTarget(new EntityTracker($$3xx, false), 0.3F, 1));
                        });
                     return true;
                  } else {
                     return false;
                  }
               }
            )
      );
   }
}
