package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;

public class TrappedChestBlockEntity extends ChestBlockEntity {
   public TrappedChestBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.TRAPPED_CHEST, $$0, $$1);
   }

   @Override
   protected void signalOpenCount(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, int $$3, int $$4) {
      super.signalOpenCount($$0, $$1, $$2, $$3, $$4);
      if ($$3 != $$4) {
         Orientation $$5 = ExperimentalRedstoneUtils.initialOrientation($$0, ((Direction)$$2.getValue(TrappedChestBlock.FACING)).getOpposite(), Direction.UP);
         Block $$6 = $$2.getBlock();
         $$0.updateNeighborsAt($$1, $$6, $$5);
         $$0.updateNeighborsAt($$1.below(), $$6, $$5);
      }
   }
}
