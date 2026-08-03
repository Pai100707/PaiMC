package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class WoodlandMansionStructure extends Structure {
   public static final MapCodec<WoodlandMansionStructure> CODEC = simpleCodec(WoodlandMansionStructure::new);

   public WoodlandMansionStructure(Structure.StructureSettings $$0) {
      super($$0);
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      Rotation $$1 = Rotation.getRandom($$0.random());
      BlockPos $$2 = this.getLowestYIn5by5BoxOffset7Blocks($$0, $$1);
      return $$2.getY() < 60
         ? Optional.empty()
         : Optional.of(new Structure.GenerationStub($$2, (Consumer<StructurePiecesBuilder>)($$3 -> this.generatePieces($$3, $$0, $$2, $$1))));
   }

   private void generatePieces(StructurePiecesBuilder $$0, Structure.GenerationContext $$1, BlockPos $$2, Rotation $$3) {
      List<WoodlandMansionPieces.WoodlandMansionPiece> $$4 = Lists.newLinkedList();
      WoodlandMansionPieces.generateMansion($$1.structureTemplateManager(), $$2, $$3, $$4, $$1.random());
      $$4.forEach($$0::addPiece);
   }

   @Override
   public void afterPlace(
      net.minecraft.world.level.WorldGenLevel $$0,
      net.minecraft.world.level.StructureManager $$1,
      ChunkGenerator $$2,
      RandomSource $$3,
      BoundingBox $$4,
      net.minecraft.world.level.ChunkPos $$5,
      PiecesContainer $$6
   ) {
      MutableBlockPos $$7 = new MutableBlockPos();
      int $$8 = $$0.getMinY();
      BoundingBox $$9 = $$6.calculateBoundingBox();
      int $$10 = $$9.minY();

      for (int $$11 = $$4.minX(); $$11 <= $$4.maxX(); $$11++) {
         for (int $$12 = $$4.minZ(); $$12 <= $$4.maxZ(); $$12++) {
            $$7.set($$11, $$10, $$12);
            if (!$$0.isEmptyBlock($$7) && $$9.isInside($$7) && $$6.isInsidePiece($$7)) {
               for (int $$13 = $$10 - 1; $$13 > $$8; $$13--) {
                  $$7.setY($$13);
                  if (!$$0.isEmptyBlock($$7) && !$$0.getBlockState($$7).liquid()) {
                     break;
                  }

                  $$0.setBlock($$7, Blocks.COBBLESTONE.defaultBlockState(), 2);
               }
            }
         }
      }
   }

   @Override
   public StructureType<?> type() {
      return StructureType.WOODLAND_MANSION;
   }
}
