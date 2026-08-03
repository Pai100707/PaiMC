package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class SeaPickleBlock extends VegetationBlock implements BonemealableBlock, SimpleWaterloggedBlock {
   public static final MapCodec<SeaPickleBlock> CODEC = simpleCodec(SeaPickleBlock::new);
   public static final int MAX_PICKLES = 4;
   public static final IntegerProperty PICKLES = BlockStateProperties.PICKLES;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE_ONE = Block.column(4.0, 0.0, 6.0);
   private static final VoxelShape SHAPE_TWO = Block.column(10.0, 0.0, 6.0);
   private static final VoxelShape SHAPE_THREE = Block.column(12.0, 0.0, 6.0);
   private static final VoxelShape SHAPE_FOUR = Block.column(12.0, 0.0, 7.0);

   @Override
   public MapCodec<SeaPickleBlock> codec() {
      return CODEC;
   }

   protected SeaPickleBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(PICKLES, 1).setValue(WATERLOGGED, true));
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = $$0.getLevel().getBlockState($$0.getClickedPos());
      if ($$1.is(this)) {
         return $$1.setValue(PICKLES, Math.min(4, $$1.getValue(PICKLES) + 1));
      } else {
         FluidState $$2 = $$0.getLevel().getFluidState($$0.getClickedPos());
         boolean $$3 = $$2.getType() == Fluids.WATER;
         return super.getStateForPlacement($$0).setValue(WATERLOGGED, $$3);
      }
   }

   public static boolean isDead(BlockState $$0) {
      return !$$0.getValue(WATERLOGGED);
   }

   @Override
   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return !$$0.getCollisionShape($$1, $$2).getFaceShape(Direction.UP).isEmpty() || $$0.isFaceSturdy($$1, $$2, Direction.UP);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      return this.mayPlaceOn($$1.getBlockState($$3), $$1, $$3);
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
      if (!$$0.canSurvive($$1, $$3)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if ($$0.getValue(WATERLOGGED)) {
            $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
         }

         return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      }
   }

   @Override
   protected boolean canBeReplaced(BlockState $$0, BlockPlaceContext $$1) {
      return !$$1.isSecondaryUseActive() && $$1.getItemInHand().is(this.asItem()) && $$0.getValue(PICKLES) < 4 ? true : super.canBeReplaced($$0, $$1);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return switch ($$0.getValue(PICKLES)) {
         case 2 -> SHAPE_TWO;
         case 3 -> SHAPE_THREE;
         case 4 -> SHAPE_FOUR;
         default -> SHAPE_ONE;
      };
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(PICKLES, WATERLOGGED);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return !isDead($$2) && $$0.getBlockState($$1.below()).is(BlockTags.CORAL_BLOCKS);
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      int $$4 = 5;
      int $$5 = 1;
      int $$6 = 2;
      int $$7 = 0;
      int $$8 = $$2.getX() - 2;
      int $$9 = 0;

      for (int $$10 = 0; $$10 < 5; $$10++) {
         for (int $$11 = 0; $$11 < $$5; $$11++) {
            int $$12 = 2 + $$2.getY() - 1;

            for (int $$13 = $$12 - 2; $$13 < $$12; $$13++) {
               BlockPos $$14 = new BlockPos($$8 + $$10, $$13, $$2.getZ() - $$9 + $$11);
               if (!$$14.equals($$2) && $$1.nextInt(6) == 0 && $$0.getBlockState($$14).is(Blocks.WATER)) {
                  BlockState $$15 = $$0.getBlockState($$14.below());
                  if ($$15.is(BlockTags.CORAL_BLOCKS)) {
                     $$0.setBlock($$14, Blocks.SEA_PICKLE.defaultBlockState().setValue(PICKLES, $$1.nextInt(4) + 1), 3);
                  }
               }
            }
         }

         if ($$7 < 2) {
            $$5 += 2;
            $$9++;
         } else {
            $$5 -= 2;
            $$9--;
         }

         $$7++;
      }

      $$0.setBlock($$2, $$3.setValue(PICKLES, 4), 2);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }
}
