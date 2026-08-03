package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VoidStartPlatformFeature extends Feature<NoneFeatureConfiguration> {
   private static final BlockPos PLATFORM_OFFSET = new BlockPos(8, 3, 8);
   private static final net.minecraft.world.level.ChunkPos PLATFORM_ORIGIN_CHUNK = new net.minecraft.world.level.ChunkPos(PLATFORM_OFFSET);
   private static final int PLATFORM_RADIUS = 16;
   private static final int PLATFORM_RADIUS_CHUNKS = 1;

   public VoidStartPlatformFeature(Codec<NoneFeatureConfiguration> $$0) {
      super($$0);
   }

   private static int checkerboardDistance(int $$0, int $$1, int $$2, int $$3) {
      return Math.max(Math.abs($$0 - $$2), Math.abs($$1 - $$3));
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      net.minecraft.world.level.ChunkPos $$2 = new net.minecraft.world.level.ChunkPos($$0.origin());
      if (checkerboardDistance($$2.x, $$2.z, PLATFORM_ORIGIN_CHUNK.x, PLATFORM_ORIGIN_CHUNK.z) > 1) {
         return true;
      } else {
         BlockPos $$3 = PLATFORM_OFFSET.atY($$0.origin().getY() + PLATFORM_OFFSET.getY());
         MutableBlockPos $$4 = new MutableBlockPos();

         for (int $$5 = $$2.getMinBlockZ(); $$5 <= $$2.getMaxBlockZ(); $$5++) {
            for (int $$6 = $$2.getMinBlockX(); $$6 <= $$2.getMaxBlockX(); $$6++) {
               if (checkerboardDistance($$3.getX(), $$3.getZ(), $$6, $$5) <= 16) {
                  $$4.set($$6, $$3.getY(), $$5);
                  if ($$4.equals($$3)) {
                     $$1.setBlock($$4, Blocks.COBBLESTONE.defaultBlockState(), 2);
                  } else {
                     $$1.setBlock($$4, Blocks.STONE.defaultBlockState(), 2);
                  }
               }
            }
         }

         return true;
      }
   }
}
