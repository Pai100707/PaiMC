package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ChorusPlantFeature extends Feature<NoneFeatureConfiguration> {
   public ChorusPlantFeature(Codec<NoneFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      BlockPos $$2 = $$0.origin();
      RandomSource $$3 = $$0.random();
      if ($$1.isEmptyBlock($$2) && $$1.getBlockState($$2.below()).is(Blocks.END_STONE)) {
         ChorusFlowerBlock.generatePlant($$1, $$2, $$3, 8);
         return true;
      } else {
         return false;
      }
   }
}
