package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class SwampHutStructure extends Structure {
   public static final MapCodec<SwampHutStructure> CODEC = simpleCodec(SwampHutStructure::new);

   public SwampHutStructure(Structure.StructureSettings $$0) {
      super($$0);
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      return onTopOfChunkCenter($$0, Heightmap.Types.WORLD_SURFACE_WG, $$1 -> generatePieces($$1, $$0));
   }

   private static void generatePieces(StructurePiecesBuilder $$0, Structure.GenerationContext $$1) {
      $$0.addPiece(new SwampHutPiece($$1.random(), $$1.chunkPos().getMinBlockX(), $$1.chunkPos().getMinBlockZ()));
   }

   @Override
   public StructureType<?> type() {
      return StructureType.SWAMP_HUT;
   }
}
