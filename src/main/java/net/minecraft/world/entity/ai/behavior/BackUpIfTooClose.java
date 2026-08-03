package net.minecraft.world.entity.ai.behavior;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class BackUpIfTooClose {
   public static OneShot<net.minecraft.world.entity.Mob> create(int $$0, float $$1) {
      return BehaviorBuilder.create(
         $$2 -> $$2.group(
               $$2.absent(MemoryModuleType.WALK_TARGET),
               $$2.registered(MemoryModuleType.LOOK_TARGET),
               $$2.present(MemoryModuleType.ATTACK_TARGET),
               $$2.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            )
            .apply($$2, ($$3, $$4, $$5, $$6) -> ($$6x, $$7, $$8) -> {
               net.minecraft.world.entity.LivingEntity $$9 = $$2.get($$5);
               if ($$9.closerThan($$7, $$0) && $$2.<NearestVisibleLivingEntities>get($$6).contains($$9)) {
                  $$4.set(new EntityTracker($$9, true));
                  $$7.getMoveControl().strafe(-$$1, 0.0F);
                  $$7.setYRot(Mth.rotateIfNecessary($$7.getYRot(), $$7.yHeadRot, 0.0F));
                  return true;
               } else {
                  return false;
               }
            })
      );
   }
}
