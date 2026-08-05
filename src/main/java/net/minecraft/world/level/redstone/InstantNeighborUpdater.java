package net.minecraft.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class InstantNeighborUpdater implements NeighborUpdater {
   private final net.minecraft.world.level.Level level;

   public InstantNeighborUpdater(net.minecraft.world.level.Level $$0) {
      this.level = $$0;
   }

   @Override
   public void shapeUpdate(Direction $$0, BlockState $$1, BlockPos $$2, BlockPos $$3, @Block.UpdateFlags int $$4, int $$5) {
      NeighborUpdater.executeShapeUpdate(this.level, $$0, $$2, $$3, $$1, $$4, $$5 - 1);
   }

   @Override
   public void neighborChanged(BlockPos $$0, Block $$1, Orientation $$2) {
      BlockState $$3 = this.level.getBlockState($$0);
      this.neighborChanged($$3, $$0, $$1, $$2, false);
   }

   @Override
   public void neighborChanged(BlockState $$0, BlockPos $$1, Block $$2, Orientation $$3, boolean $$4) {
      NeighborUpdater.executeUpdate(this.level, $$0, $$1, $$2, $$3, $$4);
   }
}
