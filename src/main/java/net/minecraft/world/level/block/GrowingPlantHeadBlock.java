package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantHeadBlock extends GrowingPlantBlock implements BonemealableBlock {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
   public static final int MAX_AGE = 25;
   private final double growPerTickProbability;

   protected GrowingPlantHeadBlock(BlockBehaviour.Properties $$0, Direction $$1, VoxelShape $$2, boolean $$3, double $$4) {
      super($$0, $$1, $$2, $$3);
      this.growPerTickProbability = $$4;
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
   }

   @Override
   protected abstract MapCodec<? extends GrowingPlantHeadBlock> codec();

   @Override
   public BlockState getStateForPlacement(RandomSource $$0) {
      return this.defaultBlockState().setValue(AGE, $$0.nextInt(25));
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return $$0.getValue(AGE) < 25;
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(AGE) < 25 && $$3.nextDouble() < this.growPerTickProbability) {
         BlockPos $$4 = $$2.relative(this.growthDirection);
         if (this.canGrowInto($$1.getBlockState($$4))) {
            $$1.setBlockAndUpdate($$4, this.getGrowIntoState($$0, $$1.random));
         }
      }
   }

   protected BlockState getGrowIntoState(BlockState $$0, RandomSource $$1) {
      return $$0.cycle(AGE);
   }

   public BlockState getMaxAgeState(BlockState $$0) {
      return $$0.setValue(AGE, 25);
   }

   public boolean isMaxAge(BlockState $$0) {
      return $$0.getValue(AGE) == 25;
   }

   protected BlockState updateBodyAfterConvertedFromHead(BlockState $$0, BlockState $$1) {
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
      if ($$4 == this.growthDirection.getOpposite()) {
         if (!$$0.canSurvive($$1, $$3)) {
            $$2.scheduleTick($$3, this, 1);
         } else {
            BlockState $$8 = $$1.getBlockState($$3.relative(this.growthDirection));
            if ($$8.is(this) || $$8.is(this.getBodyBlock())) {
               return this.updateBodyAfterConvertedFromHead($$0, this.getBodyBlock().defaultBlockState());
            }
         }
      }

      if ($$4 != this.growthDirection || !$$6.is(this) && !$$6.is(this.getBodyBlock())) {
         if (this.scheduleFluidTicks) {
            $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
         }

         return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else {
         return this.updateBodyAfterConvertedFromHead($$0, this.getBodyBlock().defaultBlockState());
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AGE);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return this.canGrowInto($$0.getBlockState($$1.relative(this.growthDirection)));
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      BlockPos $$4 = $$2.relative(this.growthDirection);
      int $$5 = Math.min($$3.getValue(AGE) + 1, 25);
      int $$6 = this.getBlocksToGrowWhenBonemealed($$1);

      for (int $$7 = 0; $$7 < $$6 && this.canGrowInto($$0.getBlockState($$4)); $$7++) {
         $$0.setBlockAndUpdate($$4, $$3.setValue(AGE, $$5));
         $$4 = $$4.relative(this.growthDirection);
         $$5 = Math.min($$5 + 1, 25);
      }
   }

   protected abstract int getBlocksToGrowWhenBonemealed(RandomSource var1);

   protected abstract boolean canGrowInto(BlockState var1);

   @Override
   protected GrowingPlantHeadBlock getHeadBlock() {
      return this;
   }
}
