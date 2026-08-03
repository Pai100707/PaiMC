package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WeepingVinesFeature extends Feature<NoneFeatureConfiguration> {
   private static final Direction[] DIRECTIONS = Direction.values();

   public WeepingVinesFeature(Codec<NoneFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      BlockPos $$2 = $$0.origin();
      RandomSource $$3 = $$0.random();
      if (!$$1.isEmptyBlock($$2)) {
         return false;
      } else {
         BlockState $$4 = $$1.getBlockState($$2.above());
         if (!$$4.is(Blocks.NETHERRACK) && !$$4.is(Blocks.NETHER_WART_BLOCK)) {
            return false;
         } else {
            this.placeRoofNetherWart($$1, $$3, $$2);
            this.placeRoofWeepingVines($$1, $$3, $$2);
            return true;
         }
      }
   }

   private void placeRoofNetherWart(net.minecraft.world.level.LevelAccessor $$0, RandomSource $$1, BlockPos $$2) {
      $$0.setBlock($$2, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
      MutableBlockPos $$3 = new MutableBlockPos();
      MutableBlockPos $$4 = new MutableBlockPos();

      for (int $$5 = 0; $$5 < 200; $$5++) {
         $$3.setWithOffset($$2, $$1.nextInt(6) - $$1.nextInt(6), $$1.nextInt(2) - $$1.nextInt(5), $$1.nextInt(6) - $$1.nextInt(6));
         if ($$0.isEmptyBlock($$3)) {
            int $$6 = 0;

            for (Direction $$7 : DIRECTIONS) {
               BlockState $$8 = $$0.getBlockState($$4.setWithOffset($$3, $$7));
               if ($$8.is(Blocks.NETHERRACK) || $$8.is(Blocks.NETHER_WART_BLOCK)) {
                  $$6++;
               }

               if ($$6 > 1) {
                  break;
               }
            }

            if ($$6 == 1) {
               $$0.setBlock($$3, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
            }
         }
      }
   }

   private void placeRoofWeepingVines(net.minecraft.world.level.LevelAccessor $$0, RandomSource $$1, BlockPos $$2) {
      MutableBlockPos $$3 = new MutableBlockPos();

      for (int $$4 = 0; $$4 < 100; $$4++) {
         $$3.setWithOffset($$2, $$1.nextInt(8) - $$1.nextInt(8), $$1.nextInt(2) - $$1.nextInt(7), $$1.nextInt(8) - $$1.nextInt(8));
         if ($$0.isEmptyBlock($$3)) {
            BlockState $$5 = $$0.getBlockState($$3.above());
            if ($$5.is(Blocks.NETHERRACK) || $$5.is(Blocks.NETHER_WART_BLOCK)) {
               int $$6 = Mth.nextInt($$1, 1, 8);
               if ($$1.nextInt(6) == 0) {
                  $$6 *= 2;
               }

               if ($$1.nextInt(5) == 0) {
                  $$6 = 1;
               }

               int $$7 = 17;
               int $$8 = 25;
               placeWeepingVinesColumn($$0, $$1, $$3, $$6, 17, 25);
            }
         }
      }
   }

   public static void placeWeepingVinesColumn(net.minecraft.world.level.LevelAccessor $$0, RandomSource $$1, MutableBlockPos $$2, int $$3, int $$4, int $$5) {
      for (int $$6 = 0; $$6 <= $$3; $$6++) {
         if ($$0.isEmptyBlock($$2)) {
            if ($$6 == $$3 || !$$0.isEmptyBlock($$2.below())) {
               $$0.setBlock($$2, Blocks.WEEPING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Mth.nextInt($$1, $$4, $$5)), 2);
               break;
            }

            $$0.setBlock($$2, Blocks.WEEPING_VINES_PLANT.defaultBlockState(), 2);
         }

         $$2.move(Direction.DOWN);
      }
   }
}
