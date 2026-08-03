package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeafLitterBlock extends VegetationBlock implements SegmentableBlock {
   public static final MapCodec<LeafLitterBlock> CODEC = simpleCodec(LeafLitterBlock::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   private final Function<BlockState, VoxelShape> shapes;

   public LeafLitterBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(this.getSegmentAmountProperty(), 1));
      this.shapes = this.makeShapes();
   }

   private Function<BlockState, VoxelShape> makeShapes() {
      return this.getShapeForEachState(this.getShapeCalculator(FACING, this.getSegmentAmountProperty()));
   }

   @Override
   protected MapCodec<LeafLitterBlock> codec() {
      return CODEC;
   }

   @Override
   public BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }

   @Override
   public boolean canBeReplaced(BlockState $$0, BlockPlaceContext $$1) {
      return this.canBeReplaced($$0, $$1, this.getSegmentAmountProperty()) ? true : super.canBeReplaced($$0, $$1);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      return $$1.getBlockState($$3).isFaceSturdy($$1, $$3, Direction.UP);
   }

   @Override
   public VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.apply($$0);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.getStateForPlacement($$0, this, this.getSegmentAmountProperty(), FACING);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, this.getSegmentAmountProperty());
   }
}
