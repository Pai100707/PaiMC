package net.minecraft.world.level.block;

import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface SegmentableBlock {
   int MIN_SEGMENT = 1;
   int MAX_SEGMENT = 4;
   IntegerProperty AMOUNT = BlockStateProperties.SEGMENT_AMOUNT;

   default Function<BlockState, VoxelShape> getShapeCalculator(EnumProperty<Direction> $$0, IntegerProperty $$1) {
      Map<Direction, VoxelShape> $$2 = Shapes.rotateHorizontal(Block.box(0.0, 0.0, 0.0, 8.0, this.getShapeHeight(), 8.0));
      return $$3 -> {
         VoxelShape $$4 = Shapes.empty();
         Direction $$5 = $$3.getValue($$0);
         int $$6 = $$3.getValue($$1);

         for (int $$7 = 0; $$7 < $$6; $$7++) {
            $$4 = Shapes.or($$4, $$2.get($$5));
            $$5 = $$5.getCounterClockWise();
         }

         return $$4.singleEncompassing();
      };
   }

   default IntegerProperty getSegmentAmountProperty() {
      return AMOUNT;
   }

   default double getShapeHeight() {
      return 1.0;
   }

   default boolean canBeReplaced(BlockState $$0, BlockPlaceContext $$1, IntegerProperty $$2) {
      return !$$1.isSecondaryUseActive() && $$1.getItemInHand().is($$0.getBlock().asItem()) && $$0.getValue($$2) < 4;
   }

   default BlockState getStateForPlacement(BlockPlaceContext $$0, Block $$1, IntegerProperty $$2, EnumProperty<Direction> $$3) {
      BlockState $$4 = $$0.getLevel().getBlockState($$0.getClickedPos());
      return $$4.is($$1)
         ? $$4.setValue($$2, Math.min(4, $$4.getValue($$2) + 1))
         : $$1.defaultBlockState().setValue($$3, $$0.getHorizontalDirection().getOpposite());
   }
}
