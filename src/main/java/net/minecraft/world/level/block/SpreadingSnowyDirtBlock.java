package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

public abstract class SpreadingSnowyDirtBlock extends SnowyDirtBlock {
   protected SpreadingSnowyDirtBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   private static boolean canBeGrass(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.above();
      BlockState $$4 = $$1.getBlockState($$3);
      if ($$4.is(Blocks.SNOW) && $$4.getValue(SnowLayerBlock.LAYERS) == 1) {
         return true;
      } else if ($$4.getFluidState().getAmount() == 8) {
         return false;
      } else {
         int $$5 = LightEngine.getLightBlockInto($$0, $$4, Direction.UP, $$4.getLightBlock());
         return $$5 < 15;
      }
   }

   @Override
   protected abstract MapCodec<? extends SpreadingSnowyDirtBlock> codec();

   private static boolean canPropagate(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.above();
      return canBeGrass($$0, $$1, $$2) && !$$1.getFluidState($$3).is(FluidTags.WATER);
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!canBeGrass($$0, $$1, $$2)) {
         $$1.setBlockAndUpdate($$2, Blocks.DIRT.defaultBlockState());
      } else {
         if ($$1.getMaxLocalRawBrightness($$2.above()) >= 9) {
            BlockState $$4 = this.defaultBlockState();

            for (int $$5 = 0; $$5 < 4; $$5++) {
               BlockPos $$6 = $$2.offset($$3.nextInt(3) - 1, $$3.nextInt(5) - 3, $$3.nextInt(3) - 1);
               if ($$1.getBlockState($$6).is(Blocks.DIRT) && canPropagate($$4, $$1, $$6)) {
                  $$1.setBlockAndUpdate($$6, $$4.setValue(SNOWY, isSnowySetting($$1.getBlockState($$6.above()))));
               }
            }
         }
      }
   }
}
