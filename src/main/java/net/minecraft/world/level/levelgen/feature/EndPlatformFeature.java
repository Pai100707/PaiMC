package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndPlatformFeature extends Feature<NoneFeatureConfiguration> {
   public EndPlatformFeature(Codec<NoneFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> $$0) {
      createEndPlatform($$0.level(), $$0.origin(), false);
      return true;
   }

   public static void createEndPlatform(net.minecraft.world.level.ServerLevelAccessor $$0, BlockPos $$1, boolean $$2) {
      MutableBlockPos $$3 = $$1.mutable();

      for (int $$4 = -2; $$4 <= 2; $$4++) {
         for (int $$5 = -2; $$5 <= 2; $$5++) {
            for (int $$6 = -1; $$6 < 3; $$6++) {
               BlockPos $$7 = $$3.set($$1).move($$5, $$6, $$4);
               Block $$8 = $$6 == -1 ? Blocks.OBSIDIAN : Blocks.AIR;
               if (!$$0.getBlockState($$7).is($$8)) {
                  if ($$2) {
                     $$0.destroyBlock($$7, true, null);
                  }

                  $$0.setBlock($$7, $$8.defaultBlockState(), 3);
               }
            }
         }
      }
   }
}
