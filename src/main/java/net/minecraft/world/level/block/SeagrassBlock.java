package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class SeagrassBlock extends VegetationBlock implements BonemealableBlock, LiquidBlockContainer {
   public static final MapCodec<SeagrassBlock> CODEC = simpleCodec(SeagrassBlock::new);
   private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 12.0);

   @Override
   public MapCodec<SeagrassBlock> codec() {
      return CODEC;
   }

   protected SeagrassBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.isFaceSturdy($$1, $$2, Direction.UP) && !$$0.is(Blocks.MAGMA_BLOCK);
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      return $$1.is(FluidTags.WATER) && $$1.getAmount() == 8 ? super.getStateForPlacement($$0) : null;
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
      BlockState $$8 = super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      if (!$$8.isAir()) {
         $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
      }

      return $$8;
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return $$0.getBlockState($$1.above()).is(Blocks.WATER);
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return Fluids.WATER.getSource(false);
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      BlockState $$4 = Blocks.TALL_SEAGRASS.defaultBlockState();
      BlockState $$5 = $$4.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
      BlockPos $$6 = $$2.above();
      $$0.setBlock($$2, $$4, 2);
      $$0.setBlock($$6, $$5, 2);
   }

   @Override
   public boolean canPlaceLiquid(@Nullable LivingEntity $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, BlockState $$3, Fluid $$4) {
      return false;
   }

   @Override
   public boolean placeLiquid(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, FluidState $$3) {
      return false;
   }
}
