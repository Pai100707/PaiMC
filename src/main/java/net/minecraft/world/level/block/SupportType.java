package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum SupportType {
   FULL {
      @Override
      public boolean isSupporting(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
         return Block.isFaceFull($$0.getBlockSupportShape($$1, $$2), $$3);
      }
   },
   CENTER {
      private final VoxelShape CENTER_SUPPORT_SHAPE = Block.column(2.0, 0.0, 10.0);

      @Override
      public boolean isSupporting(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
         return !Shapes.joinIsNotEmpty($$0.getBlockSupportShape($$1, $$2).getFaceShape($$3), this.CENTER_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
      }
   },
   RIGID {
      private final VoxelShape RIGID_SUPPORT_SHAPE = Shapes.join(Shapes.block(), Block.column(12.0, 0.0, 16.0), BooleanOp.ONLY_FIRST);

      @Override
      public boolean isSupporting(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
         return !Shapes.joinIsNotEmpty($$0.getBlockSupportShape($$1, $$2).getFaceShape($$3), this.RIGID_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
      }
   };

   public abstract boolean isSupporting(BlockState var1, net.minecraft.world.level.BlockGetter var2, BlockPos var3, Direction var4);
}
