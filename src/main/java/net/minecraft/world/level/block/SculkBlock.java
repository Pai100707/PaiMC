package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class SculkBlock extends DropExperienceBlock implements SculkBehaviour {
   public static final MapCodec<SculkBlock> CODEC = simpleCodec(SculkBlock::new);

   @Override
   public MapCodec<SculkBlock> codec() {
      return CODEC;
   }

   public SculkBlock(BlockBehaviour.Properties $$0) {
      super(ConstantInt.of(1), $$0);
   }

   @Override
   public int attemptUseCharge(
      SculkSpreader.ChargeCursor $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, RandomSource $$3, SculkSpreader $$4, boolean $$5
   ) {
      int $$6 = $$0.getCharge();
      if ($$6 != 0 && $$3.nextInt($$4.chargeDecayRate()) == 0) {
         BlockPos $$7 = $$0.getPos();
         boolean $$8 = $$7.closerThan($$2, $$4.noGrowthRadius());
         if (!$$8 && canPlaceGrowth($$1, $$7)) {
            int $$9 = $$4.growthSpawnCost();
            if ($$3.nextInt($$9) < $$6) {
               BlockPos $$10 = $$7.above();
               BlockState $$11 = this.getRandomGrowthState($$1, $$10, $$3, $$4.isWorldGeneration());
               $$1.setBlock($$10, $$11, 3);
               $$1.playSound(null, $$7, $$11.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return Math.max(0, $$6 - $$9);
         } else {
            return $$3.nextInt($$4.additionalDecayRate()) != 0 ? $$6 : $$6 - ($$8 ? 1 : getDecayPenalty($$4, $$7, $$2, $$6));
         }
      } else {
         return $$6;
      }
   }

   private static int getDecayPenalty(SculkSpreader $$0, BlockPos $$1, BlockPos $$2, int $$3) {
      int $$4 = $$0.noGrowthRadius();
      float $$5 = Mth.square((float)Math.sqrt($$1.distSqr($$2)) - $$4);
      int $$6 = Mth.square(24 - $$4);
      float $$7 = Math.min(1.0F, $$5 / $$6);
      return Math.max(1, (int)($$3 * $$7 * 0.5F));
   }

   private BlockState getRandomGrowthState(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, RandomSource $$2, boolean $$3) {
      BlockState $$4;
      if ($$2.nextInt(11) == 0) {
         $$4 = Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, $$3);
      } else {
         $$4 = Blocks.SCULK_SENSOR.defaultBlockState();
      }

      return $$4.hasProperty(BlockStateProperties.WATERLOGGED) && !$$0.getFluidState($$1).isEmpty()
         ? $$4.setValue(BlockStateProperties.WATERLOGGED, true)
         : $$4;
   }

   private static boolean canPlaceGrowth(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1.above());
      if ($$2.isAir() || $$2.is(Blocks.WATER) && $$2.getFluidState().is(Fluids.WATER)) {
         int $$3 = 0;

         for (BlockPos $$4 : BlockPos.betweenClosed($$1.offset(-4, 0, -4), $$1.offset(4, 2, 4))) {
            BlockState $$5 = $$0.getBlockState($$4);
            if ($$5.is(Blocks.SCULK_SENSOR) || $$5.is(Blocks.SCULK_SHRIEKER)) {
               $$3++;
            }

            if ($$3 > 2) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean canChangeBlockStateOnSpread() {
      return false;
   }
}
