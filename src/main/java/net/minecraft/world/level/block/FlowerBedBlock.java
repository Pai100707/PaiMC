package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerBedBlock extends VegetationBlock implements BonemealableBlock, SegmentableBlock {
   public static final MapCodec<FlowerBedBlock> CODEC = simpleCodec(FlowerBedBlock::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final IntegerProperty AMOUNT = BlockStateProperties.FLOWER_AMOUNT;
   private final Function<BlockState, VoxelShape> shapes;

   @Override
   public MapCodec<FlowerBedBlock> codec() {
      return CODEC;
   }

   protected FlowerBedBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AMOUNT, 1));
      this.shapes = this.makeShapes();
   }

   private Function<BlockState, VoxelShape> makeShapes() {
      return this.getShapeForEachState(this.getShapeCalculator(FACING, AMOUNT));
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
      return this.canBeReplaced($$0, $$1, AMOUNT) ? true : super.canBeReplaced($$0, $$1);
   }

   @Override
   public VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.apply($$0);
   }

   @Override
   public double getShapeHeight() {
      return 3.0;
   }

   @Override
   public IntegerProperty getSegmentAmountProperty() {
      return AMOUNT;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.getStateForPlacement($$0, this, AMOUNT, FACING);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, AMOUNT);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return true;
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      int $$4 = $$3.getValue(AMOUNT);
      if ($$4 < 4) {
         $$0.setBlock($$2, $$3.setValue(AMOUNT, $$4 + 1), 2);
      } else {
         popResource($$0, $$2, new ItemStack(this));
      }
   }
}
