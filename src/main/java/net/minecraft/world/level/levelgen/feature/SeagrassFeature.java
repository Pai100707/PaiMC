package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class SeagrassFeature extends Feature<ProbabilityFeatureConfiguration> {
   public SeagrassFeature(Codec<ProbabilityFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> $$0) {
      boolean $$1 = false;
      RandomSource $$2 = $$0.random();
      net.minecraft.world.level.WorldGenLevel $$3 = $$0.level();
      BlockPos $$4 = $$0.origin();
      ProbabilityFeatureConfiguration $$5 = $$0.config();
      int $$6 = $$2.nextInt(8) - $$2.nextInt(8);
      int $$7 = $$2.nextInt(8) - $$2.nextInt(8);
      int $$8 = $$3.getHeight(Heightmap.Types.OCEAN_FLOOR, $$4.getX() + $$6, $$4.getZ() + $$7);
      BlockPos $$9 = new BlockPos($$4.getX() + $$6, $$8, $$4.getZ() + $$7);
      if ($$3.getBlockState($$9).is(Blocks.WATER)) {
         boolean $$10 = $$2.nextDouble() < $$5.probability;
         BlockState $$11 = $$10 ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();
         if ($$11.canSurvive($$3, $$9)) {
            if ($$10) {
               BlockState $$12 = $$11.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
               BlockPos $$13 = $$9.above();
               if ($$3.getBlockState($$13).is(Blocks.WATER)) {
                  $$3.setBlock($$9, $$11, 2);
                  $$3.setBlock($$13, $$12, 2);
               }
            } else {
               $$3.setBlock($$9, $$11, 2);
            }

            $$1 = true;
         }
      }

      return $$1;
   }
}
