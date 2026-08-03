package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class RodBlock extends DirectionalBlock {
   private static final Map<Axis, VoxelShape> SHAPES = Shapes.rotateAllAxis(Block.cube(4.0, 4.0, 16.0));

   protected RodBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected abstract MapCodec<? extends RodBlock> codec();

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get(((Direction)$$0.getValue(FACING)).getAxis());
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.setValue(FACING, $$1.mirror($$0.getValue(FACING)));
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }
}
