package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class RingBell {
   private static final float BELL_RING_CHANCE = 0.95F;
   public static final int RING_BELL_FROM_DISTANCE = 3;

   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create() {
      return BehaviorBuilder.create($$0 -> $$0.group($$0.present(MemoryModuleType.MEETING_POINT)).apply($$0, $$1 -> ($$2, $$3, $$4) -> {
         if ($$2.random.nextFloat() <= 0.95F) {
            return false;
         } else {
            BlockPos $$5 = $$0.<GlobalPos>get($$1).pos();
            if ($$5.closerThan($$3.blockPosition(), 3.0)) {
               BlockState $$6 = $$2.getBlockState($$5);
               if ($$6.is(Blocks.BELL)) {
                  BellBlock $$7 = (BellBlock)$$6.getBlock();
                  $$7.attemptToRing($$3, $$2, $$5, null);
               }
            }

            return true;
         }
      }));
   }
}
