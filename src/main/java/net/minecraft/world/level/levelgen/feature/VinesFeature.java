package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VinesFeature extends Feature<NoneFeatureConfiguration> {
   public VinesFeature(Codec<NoneFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      BlockPos $$2 = $$0.origin();
      $$0.config();
      if (!$$1.isEmptyBlock($$2)) {
         return false;
      } else {
         for (Direction $$3 : Direction.values()) {
            if ($$3 != Direction.DOWN && VineBlock.isAcceptableNeighbour($$1, $$2.relative($$3), $$3)) {
               $$1.setBlock($$2, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace($$3), true), 2);
               return true;
            }
         }

         return false;
      }
   }
}
