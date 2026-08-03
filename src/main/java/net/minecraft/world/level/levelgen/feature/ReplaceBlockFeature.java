package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
   public ReplaceBlockFeature(Codec<ReplaceBlockConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<ReplaceBlockConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      BlockPos $$2 = $$0.origin();
      ReplaceBlockConfiguration $$3 = $$0.config();

      for (OreConfiguration.TargetBlockState $$4 : $$3.targetStates) {
         if ($$4.target.test($$1.getBlockState($$2), $$0.random())) {
            $$1.setBlock($$2, $$4.state, 2);
            break;
         }
      }

      return true;
   }
}
