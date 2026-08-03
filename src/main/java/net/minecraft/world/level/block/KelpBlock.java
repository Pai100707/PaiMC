package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class KelpBlock extends GrowingPlantHeadBlock implements LiquidBlockContainer {
   public static final MapCodec<KelpBlock> CODEC = simpleCodec(KelpBlock::new);
   private static final double GROW_PER_TICK_PROBABILITY = 0.14;
   private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 9.0);

   @Override
   public MapCodec<KelpBlock> codec() {
      return CODEC;
   }

   protected KelpBlock(BlockBehaviour.Properties $$0) {
      super($$0, Direction.UP, SHAPE, true, 0.14);
   }

   @Override
   protected boolean canGrowInto(BlockState $$0) {
      return $$0.is(Blocks.WATER);
   }

   @Override
   protected Block getBodyBlock() {
      return Blocks.KELP_PLANT;
   }

   @Override
   protected boolean canAttachTo(BlockState $$0) {
      return !$$0.is(Blocks.MAGMA_BLOCK);
   }

   @Override
   public boolean canPlaceLiquid(@Nullable LivingEntity $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, BlockState $$3, Fluid $$4) {
      return false;
   }

   @Override
   public boolean placeLiquid(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, FluidState $$3) {
      return false;
   }

   @Override
   protected int getBlocksToGrowWhenBonemealed(RandomSource $$0) {
      return 1;
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      return $$1.is(FluidTags.WATER) && $$1.getAmount() == 8 ? super.getStateForPlacement($$0) : null;
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return Fluids.WATER.getSource(false);
   }
}
