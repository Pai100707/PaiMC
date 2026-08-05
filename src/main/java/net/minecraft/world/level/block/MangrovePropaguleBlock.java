package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MangrovePropaguleBlock extends SaplingBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<MangrovePropaguleBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(TreeGrower.CODEC.fieldOf("tree").forGetter($$0x -> $$0x.treeGrower), propertiesCodec()).apply($$0, MangrovePropaguleBlock::new)
   );
   public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
   public static final int MAX_AGE = 4;
   private static final int[] SHAPE_MIN_Y = new int[]{13, 10, 7, 3, 0};
   private static final VoxelShape[] SHAPE_PER_AGE = Block.boxes(4, $$0 -> Block.column(2.0, SHAPE_MIN_Y[$$0], 16.0));
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

   @Override
   public MapCodec<MangrovePropaguleBlock> codec() {
      return CODEC;
   }

   public MangrovePropaguleBlock(TreeGrower $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
      this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0).setValue(AGE, 0).setValue(WATERLOGGED, false).setValue(HANGING, false));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(STAGE).add(AGE).add(WATERLOGGED).add(HANGING);
   }

   @Override
   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return super.mayPlaceOn($$0, $$1, $$2) || $$0.is(Blocks.CLAY);
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      boolean $$2 = $$1.getType() == Fluids.WATER;
      return super.getStateForPlacement($$0).setValue(WATERLOGGED, $$2).setValue(AGE, 4);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      int $$4 = $$0.getValue(HANGING) ? $$0.getValue(AGE) : 4;
      return SHAPE_PER_AGE[$$4].move($$0.getOffset($$2));
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return isHanging($$0) ? $$1.getBlockState($$2.above()).is(Blocks.MANGROVE_LEAVES) : super.canSurvive($$0, $$1, $$2);
   }

   @Override
   protected BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      if ($$0.getValue(WATERLOGGED)) {
         $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
      }

      return $$4 == Direction.UP && !$$0.canSurvive($$1, $$3) ? Blocks.AIR.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!isHanging($$0)) {
         if ($$3.nextInt(7) == 0) {
            this.advanceTree($$1, $$2, $$0, $$3);
         }
      } else {
         if (!isFullyGrown($$0)) {
            $$1.setBlock($$2, $$0.cycle(AGE), 2);
         }
      }
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return !isHanging($$2) || !isFullyGrown($$2);
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return isHanging($$3) ? !isFullyGrown($$3) : super.isBonemealSuccess($$0, $$1, $$2, $$3);
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      if (isHanging($$3) && !isFullyGrown($$3)) {
         $$0.setBlock($$2, $$3.cycle(AGE), 2);
      } else {
         super.performBonemeal($$0, $$1, $$2, $$3);
      }
   }

   private static boolean isHanging(BlockState $$0) {
      return $$0.getValue(HANGING);
   }

   private static boolean isFullyGrown(BlockState $$0) {
      return $$0.getValue(AGE) == 4;
   }

   public static BlockState createNewHangingPropagule() {
      return createNewHangingPropagule(0);
   }

   public static BlockState createNewHangingPropagule(int $$0) {
      return Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(HANGING, true).setValue(AGE, $$0);
   }
}
