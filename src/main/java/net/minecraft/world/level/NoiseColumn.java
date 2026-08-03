package net.minecraft.world.level;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public final class NoiseColumn implements BlockColumn {
   private final int minY;
   private final BlockState[] column;

   public NoiseColumn(int $$0, BlockState[] $$1) {
      this.minY = $$0;
      this.column = $$1;
   }

   @Override
   public BlockState getBlock(int $$0) {
      int $$1 = $$0 - this.minY;
      return $$1 >= 0 && $$1 < this.column.length ? this.column[$$1] : Blocks.AIR.defaultBlockState();
   }

   @Override
   public void setBlock(int $$0, BlockState $$1) {
      int $$2 = $$0 - this.minY;
      if ($$2 >= 0 && $$2 < this.column.length) {
         this.column[$$2] = $$1;
      } else {
         throw new IllegalArgumentException("Outside of column height: " + $$0);
      }
   }
}
