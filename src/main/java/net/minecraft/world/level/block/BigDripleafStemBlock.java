package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafStemBlock extends HorizontalDirectionalBlock implements BonemealableBlock, SimpleWaterloggedBlock {
   public static final MapCodec<BigDripleafStemBlock> CODEC = simpleCodec(BigDripleafStemBlock::new);
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.column(6.0, 0.0, 16.0).move(0.0, 0.0, 0.25).optimize());

   @Override
   public MapCodec<BigDripleafStemBlock> codec() {
      return CODEC;
   }

   protected BigDripleafStemBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get($$0.getValue(FACING));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(WATERLOGGED, FACING);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      BlockState $$4 = $$1.getBlockState($$3);
      BlockState $$5 = $$1.getBlockState($$2.above());
      return ($$4.is(this) || $$4.is(BlockTags.BIG_DRIPLEAF_PLACEABLE)) && ($$5.is(this) || $$5.is(Blocks.BIG_DRIPLEAF));
   }

   protected static boolean place(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, FluidState $$2, Direction $$3) {
      BlockState $$4 = Blocks.BIG_DRIPLEAF_STEM.defaultBlockState().setValue(WATERLOGGED, $$2.isSourceOfType(Fluids.WATER)).setValue(FACING, $$3);
      return $$0.setBlock($$1, $$4, 3);
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
      if (($$4 == Direction.DOWN || $$4 == Direction.UP) && !$$0.canSurvive($$1, $$3)) {
         $$2.scheduleTick($$3, this, 1);
      }

      if ($$0.getValue(WATERLOGGED)) {
         $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
      }

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!$$0.canSurvive($$1, $$2)) {
         $$1.destroyBlock($$2, true);
      }
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      Optional<BlockPos> $$3 = BlockUtil.getTopConnectedBlock($$0, $$1, $$2.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
      if ($$3.isEmpty()) {
         return false;
      } else {
         BlockPos $$4 = $$3.get().above();
         BlockState $$5 = $$0.getBlockState($$4);
         return BigDripleafBlock.canPlaceAt($$0, $$4, $$5);
      }
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      Optional<BlockPos> $$4 = BlockUtil.getTopConnectedBlock($$0, $$2, $$3.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
      if (!$$4.isEmpty()) {
         BlockPos $$5 = $$4.get();
         BlockPos $$6 = $$5.above();
         Direction $$7 = $$3.getValue(FACING);
         place($$0, $$5, $$0.getFluidState($$5), $$7);
         BigDripleafBlock.place($$0, $$6, $$0.getFluidState($$6), $$7);
      }
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return new ItemStack(Blocks.BIG_DRIPLEAF);
   }
}
