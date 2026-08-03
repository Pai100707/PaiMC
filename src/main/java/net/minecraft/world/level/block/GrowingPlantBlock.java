package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class GrowingPlantBlock extends Block {
   protected final Direction growthDirection;
   protected final boolean scheduleFluidTicks;
   protected final VoxelShape shape;

   protected GrowingPlantBlock(BlockBehaviour.Properties $$0, Direction $$1, VoxelShape $$2, boolean $$3) {
      super($$0);
      this.growthDirection = $$1;
      this.shape = $$2;
      this.scheduleFluidTicks = $$3;
   }

   @Override
   protected abstract MapCodec<? extends GrowingPlantBlock> codec();

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = $$0.getLevel().getBlockState($$0.getClickedPos().relative(this.growthDirection));
      return !$$1.is(this.getHeadBlock()) && !$$1.is(this.getBodyBlock())
         ? this.getStateForPlacement($$0.getLevel().random)
         : this.getBodyBlock().defaultBlockState();
   }

   public BlockState getStateForPlacement(RandomSource $$0) {
      return this.defaultBlockState();
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.relative(this.growthDirection.getOpposite());
      BlockState $$4 = $$1.getBlockState($$3);
      return !this.canAttachTo($$4) ? false : $$4.is(this.getHeadBlock()) || $$4.is(this.getBodyBlock()) || $$4.isFaceSturdy($$1, $$3, this.growthDirection);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!$$0.canSurvive($$1, $$2)) {
         $$1.destroyBlock($$2, true);
      }
   }

   protected boolean canAttachTo(BlockState $$0) {
      return true;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shape;
   }

   protected abstract GrowingPlantHeadBlock getHeadBlock();

   protected abstract Block getBodyBlock();
}
