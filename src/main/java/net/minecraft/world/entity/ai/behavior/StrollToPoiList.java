package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollToPoiList {
   public static BehaviorControl<Villager> create(MemoryModuleType<List<GlobalPos>> $$0, float $$1, int $$2, int $$3, MemoryModuleType<GlobalPos> $$4) {
      MutableLong $$5 = new MutableLong(0L);
      return BehaviorBuilder.create(
         $$6 -> $$6.group($$6.registered(MemoryModuleType.WALK_TARGET), $$6.present($$0), $$6.present($$4))
            .apply($$6, ($$5xx, $$6x, $$7) -> ($$8, $$9, $$10) -> {
               List<GlobalPos> $$11 = $$6.get($$6x);
               GlobalPos $$12 = $$6.get($$7);
               if ($$11.isEmpty()) {
                  return false;
               } else {
                  GlobalPos $$13 = $$11.get($$8.getRandom().nextInt($$11.size()));
                  if ($$13 != null && $$8.dimension() == $$13.dimension() && $$12.pos().closerToCenterThan($$9.position(), $$3)) {
                     if ($$10 > $$5.longValue()) {
                        $$5xx.set(new WalkTarget($$13.pos(), $$1, $$2));
                        $$5.setValue($$10 + 100L);
                     }

                     return true;
                  } else {
                     return false;
                  }
               }
            })
      );
   }
}
