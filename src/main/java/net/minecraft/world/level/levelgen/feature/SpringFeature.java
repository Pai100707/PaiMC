package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;

public class SpringFeature extends Feature<SpringConfiguration> {
   public SpringFeature(Codec<SpringConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<SpringConfiguration> $$0) {
      SpringConfiguration $$1 = $$0.config();
      net.minecraft.world.level.WorldGenLevel $$2 = $$0.level();
      BlockPos $$3 = $$0.origin();
      if (!$$2.getBlockState($$3.above()).is($$1.validBlocks)) {
         return false;
      } else if ($$1.requiresBlockBelow && !$$2.getBlockState($$3.below()).is($$1.validBlocks)) {
         return false;
      } else {
         BlockState $$4 = $$2.getBlockState($$3);
         if (!$$4.isAir() && !$$4.is($$1.validBlocks)) {
            return false;
         } else {
            int $$5 = 0;
            int $$6 = 0;
            if ($$2.getBlockState($$3.west()).is($$1.validBlocks)) {
               $$6++;
            }

            if ($$2.getBlockState($$3.east()).is($$1.validBlocks)) {
               $$6++;
            }

            if ($$2.getBlockState($$3.north()).is($$1.validBlocks)) {
               $$6++;
            }

            if ($$2.getBlockState($$3.south()).is($$1.validBlocks)) {
               $$6++;
            }

            if ($$2.getBlockState($$3.below()).is($$1.validBlocks)) {
               $$6++;
            }

            int $$7 = 0;
            if ($$2.isEmptyBlock($$3.west())) {
               $$7++;
            }

            if ($$2.isEmptyBlock($$3.east())) {
               $$7++;
            }

            if ($$2.isEmptyBlock($$3.north())) {
               $$7++;
            }

            if ($$2.isEmptyBlock($$3.south())) {
               $$7++;
            }

            if ($$2.isEmptyBlock($$3.below())) {
               $$7++;
            }

            if ($$6 == $$1.rockCount && $$7 == $$1.holeCount) {
               $$2.setBlock($$3, $$1.state.createLegacyBlock(), 2);
               $$2.scheduleTick($$3, $$1.state.getType(), 0);
               $$5++;
            }

            return $$5 > 0;
         }
      }
   }
}
