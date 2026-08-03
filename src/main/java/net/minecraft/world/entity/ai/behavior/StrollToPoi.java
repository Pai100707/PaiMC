package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollToPoi {
   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> create(MemoryModuleType<GlobalPos> $$0, float $$1, int $$2, int $$3) {
      MutableLong $$4 = new MutableLong(0L);
      return BehaviorBuilder.create(
         $$5 -> $$5.group($$5.registered(MemoryModuleType.WALK_TARGET), $$5.present($$0)).apply($$5, ($$5x, $$6) -> ($$7, $$8, $$9) -> {
            GlobalPos $$10 = $$5.get($$6);
            if ($$7.dimension() != $$10.dimension() || !$$10.pos().closerToCenterThan($$8.position(), $$3)) {
               return false;
            } else if ($$9 <= $$4.longValue()) {
               return true;
            } else {
               $$5x.set(new WalkTarget($$10.pos(), $$1, $$2));
               $$4.setValue($$9 + 80L);
               return true;
            }
         })
      );
   }
}
