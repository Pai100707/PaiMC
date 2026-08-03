package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanMonumentStructure extends Structure {
   public static final MapCodec<OceanMonumentStructure> CODEC = simpleCodec(OceanMonumentStructure::new);

   public OceanMonumentStructure(Structure.StructureSettings $$0) {
      super($$0);
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      int $$1 = $$0.chunkPos().getBlockX(9);
      int $$2 = $$0.chunkPos().getBlockZ(9);

      for (Holder<Biome> $$4 : $$0.biomeSource().getBiomesWithin($$1, $$0.chunkGenerator().getSeaLevel(), $$2, 29, $$0.randomState().sampler())) {
         if (!$$4.is(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING)) {
            return Optional.empty();
         }
      }

      return onTopOfChunkCenter($$0, Heightmap.Types.OCEAN_FLOOR_WG, $$1x -> generatePieces($$1x, $$0));
   }

   private static StructurePiece createTopPiece(net.minecraft.world.level.ChunkPos $$0, WorldgenRandom $$1) {
      int $$2 = $$0.getMinBlockX() - 29;
      int $$3 = $$0.getMinBlockZ() - 29;
      Direction $$4 = Plane.HORIZONTAL.getRandomDirection($$1);
      return new OceanMonumentPieces.MonumentBuilding($$1, $$2, $$3, $$4);
   }

   private static void generatePieces(StructurePiecesBuilder $$0, Structure.GenerationContext $$1) {
      $$0.addPiece(createTopPiece($$1.chunkPos(), $$1.random()));
   }

   public static PiecesContainer regeneratePiecesAfterLoad(net.minecraft.world.level.ChunkPos $$0, long $$1, PiecesContainer $$2) {
      if ($$2.isEmpty()) {
         return $$2;
      } else {
         WorldgenRandom $$3 = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
         $$3.setLargeFeatureSeed($$1, $$0.x, $$0.z);
         StructurePiece $$4 = $$2.pieces().get(0);
         BoundingBox $$5 = $$4.getBoundingBox();
         int $$6 = $$5.minX();
         int $$7 = $$5.minZ();
         Direction $$8 = Plane.HORIZONTAL.getRandomDirection($$3);
         Direction $$9 = Objects.requireNonNullElse($$4.getOrientation(), $$8);
         StructurePiece $$10 = new OceanMonumentPieces.MonumentBuilding($$3, $$6, $$7, $$9);
         StructurePiecesBuilder $$11 = new StructurePiecesBuilder();
         $$11.addPiece($$10);
         return $$11.build();
      }
   }

   @Override
   public StructureType<?> type() {
      return StructureType.OCEAN_MONUMENT;
   }
}
