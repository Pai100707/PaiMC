package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBodyBlock extends GrowingPlantBlock implements BonemealableBlock {
   protected GrowingPlantBodyBlock(BlockBehaviour.Properties $$0, Direction $$1, VoxelShape $$2, boolean $$3) {
      super($$0, $$1, $$2, $$3);
   }

   @Override
   protected abstract MapCodec<? extends GrowingPlantBodyBlock> codec();

   protected BlockState updateHeadAfterConvertedFromBody(BlockState $$0, BlockState $$1) {
      return $$1;
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
      if ($$4 == this.growthDirection.getOpposite() && !$$0.canSurvive($$1, $$3)) {
         $$2.scheduleTick($$3, this, 1);
      }

      GrowingPlantHeadBlock $$8 = this.getHeadBlock();
      if ($$4 == this.growthDirection && !$$6.is(this) && !$$6.is($$8)) {
         return this.updateHeadAfterConvertedFromBody($$0, $$8.getStateForPlacement($$7));
      } else {
         if (this.scheduleFluidTicks) {
            $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
         }

         return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      }
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return new ItemStack(this.getHeadBlock());
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      Optional<BlockPos> $$3 = this.getHeadPos($$0, $$1, $$2.getBlock());
      return $$3.isPresent() && this.getHeadBlock().canGrowInto($$0.getBlockState($$3.get().relative(this.growthDirection)));
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      Optional<BlockPos> $$4 = this.getHeadPos($$0, $$2, $$3.getBlock());
      if ($$4.isPresent()) {
         BlockState $$5 = $$0.getBlockState($$4.get());
         ((GrowingPlantHeadBlock)$$5.getBlock()).performBonemeal($$0, $$1, $$4.get(), $$5);
      }
   }

   private Optional<BlockPos> getHeadPos(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Block $$2) {
      return BlockUtil.getTopConnectedBlock($$0, $$1, $$2, this.growthDirection, this.getHeadBlock());
   }

   @Override
   protected boolean canBeReplaced(BlockState $$0, BlockPlaceContext $$1) {
      boolean $$2 = super.canBeReplaced($$0, $$1);
      return $$2 && $$1.getItemInHand().is(this.getHeadBlock().asItem()) ? false : $$2;
   }

   @Override
   protected Block getBodyBlock() {
      return this;
   }
}
