package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface SignalGetter extends net.minecraft.world.level.BlockGetter {
   Direction[] DIRECTIONS = Direction.values();

   default int getDirectSignal(BlockPos $$0, Direction $$1) {
      return this.getBlockState($$0).getDirectSignal(this, $$0, $$1);
   }

   default int getDirectSignalTo(BlockPos $$0) {
      int $$1 = 0;
      $$1 = Math.max($$1, this.getDirectSignal($$0.below(), Direction.DOWN));
      if ($$1 >= 15) {
         return $$1;
      } else {
         $$1 = Math.max($$1, this.getDirectSignal($$0.above(), Direction.UP));
         if ($$1 >= 15) {
            return $$1;
         } else {
            $$1 = Math.max($$1, this.getDirectSignal($$0.north(), Direction.NORTH));
            if ($$1 >= 15) {
               return $$1;
            } else {
               $$1 = Math.max($$1, this.getDirectSignal($$0.south(), Direction.SOUTH));
               if ($$1 >= 15) {
                  return $$1;
               } else {
                  $$1 = Math.max($$1, this.getDirectSignal($$0.west(), Direction.WEST));
                  if ($$1 >= 15) {
                     return $$1;
                  } else {
                     $$1 = Math.max($$1, this.getDirectSignal($$0.east(), Direction.EAST));
                     return $$1 >= 15 ? $$1 : $$1;
                  }
               }
            }
         }
      }
   }

   default int getControlInputSignal(BlockPos $$0, Direction $$1, boolean $$2) {
      BlockState $$3 = this.getBlockState($$0);
      if ($$2) {
         return DiodeBlock.isDiode($$3) ? this.getDirectSignal($$0, $$1) : 0;
      } else if ($$3.is(Blocks.REDSTONE_BLOCK)) {
         return 15;
      } else if ($$3.is(Blocks.REDSTONE_WIRE)) {
         return $$3.getValue(RedStoneWireBlock.POWER);
      } else {
         return $$3.isSignalSource() ? this.getDirectSignal($$0, $$1) : 0;
      }
   }

   default boolean hasSignal(BlockPos $$0, Direction $$1) {
      return this.getSignal($$0, $$1) > 0;
   }

   default int getSignal(BlockPos $$0, Direction $$1) {
      BlockState $$2 = this.getBlockState($$0);
      int $$3 = $$2.getSignal(this, $$0, $$1);
      return $$2.isRedstoneConductor(this, $$0) ? Math.max($$3, this.getDirectSignalTo($$0)) : $$3;
   }

   default boolean hasNeighborSignal(BlockPos $$0) {
      if (this.getSignal($$0.below(), Direction.DOWN) > 0) {
         return true;
      } else if (this.getSignal($$0.above(), Direction.UP) > 0) {
         return true;
      } else if (this.getSignal($$0.north(), Direction.NORTH) > 0) {
         return true;
      } else if (this.getSignal($$0.south(), Direction.SOUTH) > 0) {
         return true;
      } else {
         return this.getSignal($$0.west(), Direction.WEST) > 0 ? true : this.getSignal($$0.east(), Direction.EAST) > 0;
      }
   }

   default int getBestNeighborSignal(BlockPos $$0) {
      int $$1 = 0;

      for (Direction $$2 : DIRECTIONS) {
         int $$3 = this.getSignal($$0.relative($$2), $$2);
         if ($$3 >= 15) {
            return 15;
         }

         if ($$3 > $$1) {
            $$1 = $$3;
         }
      }

      return $$1;
   }
}
