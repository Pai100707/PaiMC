package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public abstract class VegetationBlock extends Block {
   protected VegetationBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected abstract MapCodec<? extends VegetationBlock> codec();

   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.is(BlockTags.DIRT) || $$0.is(Blocks.FARMLAND);
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
      return !$$0.canSurvive($$1, $$3) ? Blocks.AIR.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      return this.mayPlaceOn($$1.getBlockState($$3), $$1, $$3);
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState $$0) {
      return $$0.getFluidState().isEmpty();
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return $$1 == PathComputationType.AIR && !this.hasCollision ? true : super.isPathfindable($$0, $$1);
   }
}
