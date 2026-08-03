package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GlowstoneFeature extends Feature<NoneFeatureConfiguration> {
   public GlowstoneFeature(Codec<NoneFeatureConfiguration> $$0) {
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
         if (!$$4.is(Blocks.NETHERRACK) && !$$4.is(Blocks.BASALT) && !$$4.is(Blocks.BLACKSTONE)) {
            return false;
         } else {
            $$1.setBlock($$2, Blocks.GLOWSTONE.defaultBlockState(), 2);

            for (int $$5 = 0; $$5 < 1500; $$5++) {
               BlockPos $$6 = $$2.offset($$3.nextInt(8) - $$3.nextInt(8), -$$3.nextInt(12), $$3.nextInt(8) - $$3.nextInt(8));
               if ($$1.getBlockState($$6).isAir()) {
                  int $$7 = 0;

                  for (Direction $$8 : Direction.values()) {
                     if ($$1.getBlockState($$6.relative($$8)).is(Blocks.GLOWSTONE)) {
                        $$7++;
                     }

                     if ($$7 > 1) {
                        break;
                     }
                  }

                  if ($$7 == 1) {
                     $$1.setBlock($$6, Blocks.GLOWSTONE.defaultBlockState(), 2);
                  }
               }
            }

            return true;
         }
      }
   }
}
