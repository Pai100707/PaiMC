package net.minecraft.world.level.levelgen.structure;

import java.util.Optional;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public abstract class SinglePieceStructure extends Structure {
   private final SinglePieceStructure.PieceConstructor constructor;
   private final int width;
   private final int depth;

   protected SinglePieceStructure(SinglePieceStructure.PieceConstructor $$0, int $$1, int $$2, Structure.StructureSettings $$3) {
      super($$3);
      this.constructor = $$0;
      this.width = $$1;
      this.depth = $$2;
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      return getLowestY($$0, this.width, this.depth) < $$0.chunkGenerator().getSeaLevel()
         ? Optional.empty()
         : onTopOfChunkCenter($$0, Heightmap.Types.WORLD_SURFACE_WG, $$1 -> this.generatePieces($$1, $$0));
   }

   private void generatePieces(StructurePiecesBuilder $$0, Structure.GenerationContext $$1) {
      net.minecraft.world.level.ChunkPos $$2 = $$1.chunkPos();
      $$0.addPiece(this.constructor.construct($$1.random(), $$2.getMinBlockX(), $$2.getMinBlockZ()));
   }

   @FunctionalInterface
   protected interface PieceConstructor {
      StructurePiece construct(WorldgenRandom var1, int var2, int var3);
   }
}
