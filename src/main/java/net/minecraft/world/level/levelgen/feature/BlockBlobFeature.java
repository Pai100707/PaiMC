package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class BlockBlobFeature extends Feature<BlockStateConfiguration> {
   public BlockBlobFeature(Codec<BlockStateConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<BlockStateConfiguration> $$0) {
      BlockPos $$1 = $$0.origin();
      net.minecraft.world.level.WorldGenLevel $$2 = $$0.level();
      RandomSource $$3 = $$0.random();

      BlockStateConfiguration $$4;
      for ($$4 = $$0.config(); $$1.getY() > $$2.getMinY() + 3; $$1 = $$1.below()) {
         if (!$$2.isEmptyBlock($$1.below())) {
            BlockState $$5 = $$2.getBlockState($$1.below());
            if (isDirt($$5) || isStone($$5)) {
               break;
            }
         }
      }

      if ($$1.getY() <= $$2.getMinY() + 3) {
         return false;
      } else {
         for (int $$6 = 0; $$6 < 3; $$6++) {
            int $$7 = $$3.nextInt(2);
            int $$8 = $$3.nextInt(2);
            int $$9 = $$3.nextInt(2);
            float $$10 = ($$7 + $$8 + $$9) * 0.333F + 0.5F;

            for (BlockPos $$11 : BlockPos.betweenClosed($$1.offset(-$$7, -$$8, -$$9), $$1.offset($$7, $$8, $$9))) {
               if ($$11.distSqr($$1) <= $$10 * $$10) {
                  $$2.setBlock($$11, $$4.state, 3);
               }
            }

            $$1 = $$1.offset(-1 + $$3.nextInt(2), -$$3.nextInt(2), -1 + $$3.nextInt(2));
         }

         return true;
      }
   }
}
