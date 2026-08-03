package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomSelectorFeature extends Feature<SimpleRandomFeatureConfiguration> {
   public SimpleRandomSelectorFeature(Codec<SimpleRandomFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<SimpleRandomFeatureConfiguration> $$0) {
      RandomSource $$1 = $$0.random();
      SimpleRandomFeatureConfiguration $$2 = $$0.config();
      net.minecraft.world.level.WorldGenLevel $$3 = $$0.level();
      BlockPos $$4 = $$0.origin();
      ChunkGenerator $$5 = $$0.chunkGenerator();
      int $$6 = $$1.nextInt($$2.features.size());
      PlacedFeature $$7 = (PlacedFeature)$$2.features.get($$6).value();
      return $$7.place($$3, $$5, $$1, $$4);
   }
}
