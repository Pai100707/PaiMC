package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class BaseCoralPlantTypeBlock extends Block implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 4.0);

   protected BaseCoralPlantTypeBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, true));
   }

   @Override
   protected abstract MapCodec<? extends BaseCoralPlantTypeBlock> codec();

   protected void tryScheduleDieTick(
      BlockState $$0, net.minecraft.world.level.BlockGetter $$1, net.minecraft.world.level.ScheduledTickAccess $$2, RandomSource $$3, BlockPos $$4
   ) {
      if (!scanForWater($$0, $$1, $$4)) {
         $$2.scheduleTick($$4, this, 60 + $$3.nextInt(40));
      }
   }

   protected static boolean scanForWater(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      if ($$0.getValue(WATERLOGGED)) {
         return true;
      } else {
         for (Direction $$3 : Direction.values()) {
            if ($$1.getFluidState($$2.relative($$3)).is(FluidTags.WATER)) {
               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      return this.defaultBlockState().setValue(WATERLOGGED, $$1.is(FluidTags.WATER) && $$1.getAmount() == 8);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
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

      return $$4 == Direction.DOWN && !this.canSurvive($$0, $$1, $$3)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      return $$1.getBlockState($$3).isFaceSturdy($$1, $$3, Direction.UP);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(WATERLOGGED);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }
}
