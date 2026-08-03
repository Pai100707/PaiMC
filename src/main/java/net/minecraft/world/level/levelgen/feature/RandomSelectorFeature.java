package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomSelectorFeature extends Feature<RandomFeatureConfiguration> {
   public RandomSelectorFeature(Codec<RandomFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<RandomFeatureConfiguration> $$0) {
      RandomFeatureConfiguration $$1 = $$0.config();
      RandomSource $$2 = $$0.random();
      net.minecraft.world.level.WorldGenLevel $$3 = $$0.level();
      ChunkGenerator $$4 = $$0.chunkGenerator();
      BlockPos $$5 = $$0.origin();

      for (WeightedPlacedFeature $$6 : $$1.features) {
         if ($$2.nextFloat() < $$6.chance) {
            return $$6.place($$3, $$4, $$2, $$5);
         }
      }

      return ((PlacedFeature)$$1.defaultFeature.value()).place($$3, $$4, $$2, $$5);
   }
}
