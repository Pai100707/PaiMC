package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public class BarrierBlock extends Block implements SimpleWaterloggedBlock {
   public static final MapCodec<BarrierBlock> CODEC = simpleCodec(BarrierBlock::new);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

   @Override
   public MapCodec<BarrierBlock> codec() {
      return CODEC;
   }

   protected BarrierBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState $$0) {
      return $$0.getFluidState().isEmpty();
   }

   @Override
   protected RenderShape getRenderShape(BlockState $$0) {
      return RenderShape.INVISIBLE;
   }

   @Override
   protected float getShadeBrightness(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return 1.0F;
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

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(WATERLOGGED, $$0.getLevel().getFluidState($$0.getClickedPos()).getType() == Fluids.WATER);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(WATERLOGGED);
   }

   @Override
   public ItemStack pickupBlock(@Nullable LivingEntity $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, BlockState $$3) {
      return $$0 instanceof Player $$4 && $$4.isCreative() ? SimpleWaterloggedBlock.super.pickupBlock($$0, $$1, $$2, $$3) : ItemStack.EMPTY;
   }

   @Override
   public boolean canPlaceLiquid(@Nullable LivingEntity $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, BlockState $$3, Fluid $$4) {
      return $$0 instanceof Player $$5 && $$5.isCreative() ? SimpleWaterloggedBlock.super.canPlaceLiquid($$0, $$1, $$2, $$3, $$4) : false;
   }
}
