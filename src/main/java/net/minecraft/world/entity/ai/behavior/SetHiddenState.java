package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.apache.commons.lang3.mutable.MutableInt;

public class SetHiddenState {
   private static final int HIDE_TIMEOUT = 300;

   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(int $$0, int $$1) {
      int $$2 = $$0 * 20;
      MutableInt $$3 = new MutableInt(0);
      return BehaviorBuilder.create(
         $$3x -> $$3x.group($$3x.present(MemoryModuleType.HIDING_PLACE), $$3x.present(MemoryModuleType.HEARD_BELL_TIME))
            .apply($$3x, ($$4, $$5) -> ($$6, $$7, $$8) -> {
               long $$9 = $$3x.<Long>get($$5);
               boolean $$10 = $$9 + 300L <= $$8;
               if ($$3.intValue() <= $$2 && !$$10) {
                  BlockPos $$11 = $$3x.<GlobalPos>get($$4).pos();
                  if ($$11.closerThan($$7.blockPosition(), $$1)) {
                     $$3.increment();
                  }

                  return true;
               } else {
                  $$5.erase();
                  $$4.erase();
                  $$7.getBrain().updateActivityFromSchedule($$6.environmentAttributes(), $$6.getGameTime(), $$7.position());
                  $$3.setValue(0);
                  return true;
               }
            })
      );
   }
}
