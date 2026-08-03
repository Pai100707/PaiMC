package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfiguration> {
   public RandomBooleanSelectorFeature(Codec<RandomBooleanFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<RandomBooleanFeatureConfiguration> $$0) {
      RandomSource $$1 = $$0.random();
      RandomBooleanFeatureConfiguration $$2 = $$0.config();
      net.minecraft.world.level.WorldGenLevel $$3 = $$0.level();
      ChunkGenerator $$4 = $$0.chunkGenerator();
      BlockPos $$5 = $$0.origin();
      boolean $$6 = $$1.nextBoolean();
      return ((PlacedFeature)($$6 ? $$2.featureTrue : $$2.featureFalse).value()).place($$3, $$4, $$1, $$5);
   }
}
