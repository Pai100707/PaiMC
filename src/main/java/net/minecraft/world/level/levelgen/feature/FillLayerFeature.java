package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;

public class FillLayerFeature extends Feature<LayerConfiguration> {
   public FillLayerFeature(Codec<LayerConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<LayerConfiguration> $$0) {
      BlockPos $$1 = $$0.origin();
      LayerConfiguration $$2 = $$0.config();
      net.minecraft.world.level.WorldGenLevel $$3 = $$0.level();
      MutableBlockPos $$4 = new MutableBlockPos();

      for (int $$5 = 0; $$5 < 16; $$5++) {
         for (int $$6 = 0; $$6 < 16; $$6++) {
            int $$7 = $$1.getX() + $$5;
            int $$8 = $$1.getZ() + $$6;
            int $$9 = $$3.getMinY() + $$2.height;
            $$4.set($$7, $$9, $$8);
            if ($$3.getBlockState($$4).isAir()) {
               $$3.setBlock($$4, $$2.state, 2);
            }
         }
      }

      return true;
   }
}
