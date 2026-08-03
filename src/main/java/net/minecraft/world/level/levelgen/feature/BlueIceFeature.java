package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BlueIceFeature extends Feature<NoneFeatureConfiguration> {
   public BlueIceFeature(Codec<NoneFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> $$0) {
      BlockPos $$1 = $$0.origin();
      net.minecraft.world.level.WorldGenLevel $$2 = $$0.level();
      RandomSource $$3 = $$0.random();
      if ($$1.getY() > $$2.getSeaLevel() - 1) {
         return false;
      } else if (!$$2.getBlockState($$1).is(Blocks.WATER) && !$$2.getBlockState($$1.below()).is(Blocks.WATER)) {
         return false;
      } else {
         boolean $$4 = false;

         for (Direction $$5 : Direction.values()) {
            if ($$5 != Direction.DOWN && $$2.getBlockState($$1.relative($$5)).is(Blocks.PACKED_ICE)) {
               $$4 = true;
               break;
            }
         }

         if (!$$4) {
            return false;
         } else {
            $$2.setBlock($$1, Blocks.BLUE_ICE.defaultBlockState(), 2);

            for (int $$6 = 0; $$6 < 200; $$6++) {
               int $$7 = $$3.nextInt(5) - $$3.nextInt(6);
               int $$8 = 3;
               if ($$7 < 2) {
                  $$8 += $$7 / 2;
               }

               if ($$8 >= 1) {
                  BlockPos $$9 = $$1.offset($$3.nextInt($$8) - $$3.nextInt($$8), $$7, $$3.nextInt($$8) - $$3.nextInt($$8));
                  BlockState $$10 = $$2.getBlockState($$9);
                  if ($$10.isAir() || $$10.is(Blocks.WATER) || $$10.is(Blocks.PACKED_ICE) || $$10.is(Blocks.ICE)) {
                     for (Direction $$11 : Direction.values()) {
                        BlockState $$12 = $$2.getBlockState($$9.relative($$11));
                        if ($$12.is(Blocks.BLUE_ICE)) {
                           $$2.setBlock($$9, Blocks.BLUE_ICE.defaultBlockState(), 2);
                           break;
                        }
                     }
                  }
               }
            }

            return true;
         }
      }
   }
}
