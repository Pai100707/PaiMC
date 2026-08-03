package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
   int SCAN_DISTANCE = 4;

   Optional<BlockState> getNext(BlockState var1);

   float getChanceModifier();

   default void changeOverTime(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      float $$4 = 0.05688889F;
      if ($$3.nextFloat() < 0.05688889F) {
         this.getNextState($$0, $$1, $$2, $$3).ifPresent($$2x -> $$1.setBlockAndUpdate($$2, $$2x));
      }
   }

   T getAge();

   default Optional<BlockState> getNextState(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      int $$4 = this.getAge().ordinal();
      int $$5 = 0;
      int $$6 = 0;

      for (BlockPos $$7 : BlockPos.withinManhattan($$2, 4, 4, 4)) {
         int $$8 = $$7.distManhattan($$2);
         if ($$8 > 4) {
            break;
         }

         if (!$$7.equals($$2) && $$1.getBlockState($$7).getBlock() instanceof ChangeOverTimeBlock<?> $$9) {
            Enum<?> $$10 = $$9.getAge();
            if (this.getAge().getClass() == $$10.getClass()) {
               int $$11 = $$10.ordinal();
               if ($$11 < $$4) {
                  return Optional.empty();
               }

               if ($$11 > $$4) {
                  $$6++;
               } else {
                  $$5++;
               }
            }
         }
      }

      float $$12 = (float)($$6 + 1) / ($$6 + $$5 + 1);
      float $$13 = $$12 * $$12 * this.getChanceModifier();
      return $$3.nextFloat() < $$13 ? this.getNext($$0) : Optional.empty();
   }
}
