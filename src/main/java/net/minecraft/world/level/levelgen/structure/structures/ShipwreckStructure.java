package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class ShipwreckStructure extends Structure {
   public static final MapCodec<ShipwreckStructure> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(settingsCodec($$0), Codec.BOOL.fieldOf("is_beached").forGetter($$0x -> $$0x.isBeached)).apply($$0, ShipwreckStructure::new)
   );
   public final boolean isBeached;

   public ShipwreckStructure(Structure.StructureSettings $$0, boolean $$1) {
      super($$0);
      this.isBeached = $$1;
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      Heightmap.Types $$1 = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
      return onTopOfChunkCenter($$0, $$1, $$1x -> this.generatePieces($$1x, $$0));
   }

   private void generatePieces(StructurePiecesBuilder $$0, Structure.GenerationContext $$1) {
      Rotation $$2 = Rotation.getRandom($$1.random());
      BlockPos $$3 = new BlockPos($$1.chunkPos().getMinBlockX(), 90, $$1.chunkPos().getMinBlockZ());
      ShipwreckPieces.ShipwreckPiece $$4 = ShipwreckPieces.addRandomPiece($$1.structureTemplateManager(), $$3, $$2, $$0, $$1.random(), this.isBeached);
      if ($$4.isTooBigToFitInWorldGenRegion()) {
         BoundingBox $$5 = $$4.getBoundingBox();
         int $$7;
         if (this.isBeached) {
            int $$6 = Structure.getLowestY($$1, $$5.minX(), $$5.getXSpan(), $$5.minZ(), $$5.getZSpan());
            $$7 = $$4.calculateBeachedPosition($$6, $$1.random());
         } else {
            $$7 = Structure.getMeanFirstOccupiedHeight($$1, $$5.minX(), $$5.getXSpan(), $$5.minZ(), $$5.getZSpan());
         }

         $$4.adjustPositionHeight($$7);
      }
   }

   @Override
   public StructureType<?> type() {
      return StructureType.SHIPWRECK;
   }
}
