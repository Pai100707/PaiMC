package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class BuriedTreasureStructure extends Structure {
   public static final MapCodec<BuriedTreasureStructure> CODEC = simpleCodec(BuriedTreasureStructure::new);

   public BuriedTreasureStructure(Structure.StructureSettings $$0) {
      super($$0);
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      return onTopOfChunkCenter($$0, Heightmap.Types.OCEAN_FLOOR_WG, $$1 -> generatePieces($$1, $$0));
   }

   private static void generatePieces(StructurePiecesBuilder $$0, Structure.GenerationContext $$1) {
      BlockPos $$2 = new BlockPos($$1.chunkPos().getBlockX(9), 90, $$1.chunkPos().getBlockZ(9));
      $$0.addPiece(new BuriedTreasurePieces.BuriedTreasurePiece($$2));
   }

   @Override
   public StructureType<?> type() {
      return StructureType.BURIED_TREASURE;
   }
}
