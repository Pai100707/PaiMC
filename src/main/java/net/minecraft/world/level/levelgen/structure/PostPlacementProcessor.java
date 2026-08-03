package net.minecraft.world.level.levelgen.structure;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

@FunctionalInterface
public interface PostPlacementProcessor {
   PostPlacementProcessor NONE = ($$0, $$1, $$2, $$3, $$4, $$5, $$6) -> {};

   void afterPlace(
      net.minecraft.world.level.WorldGenLevel var1,
      net.minecraft.world.level.StructureManager var2,
      ChunkGenerator var3,
      RandomSource var4,
      BoundingBox var5,
      net.minecraft.world.level.ChunkPos var6,
      PiecesContainer var7
   );
}
