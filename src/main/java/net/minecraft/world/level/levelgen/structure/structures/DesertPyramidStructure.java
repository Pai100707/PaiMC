package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertPyramidStructure extends SinglePieceStructure {
   public static final MapCodec<DesertPyramidStructure> CODEC = simpleCodec(DesertPyramidStructure::new);

   public DesertPyramidStructure(Structure.StructureSettings $$0) {
      super(DesertPyramidPiece::new, 21, 21, $$0);
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
      Set<BlockPos> $$7 = SortedArraySet.create(Vec3i::compareTo);

      for (StructurePiece $$8 : $$6.pieces()) {
         if ($$8 instanceof DesertPyramidPiece $$9) {
            $$7.addAll($$9.getPotentialSuspiciousSandWorldPositions());
            placeSuspiciousSand($$4, $$0, $$9.getRandomCollapsedRoofPos());
         }
      }

      ObjectArrayList<BlockPos> $$10 = new ObjectArrayList($$7.stream().toList());
      RandomSource $$11 = RandomSource.create($$0.getSeed()).forkPositional().at($$6.calculateBoundingBox().getCenter());
      Util.shuffle($$10, $$11);
      int $$12 = Math.min($$7.size(), $$11.nextInt(5, 8));
      ObjectListIterator var12 = $$10.iterator();

      while (var12.hasNext()) {
         BlockPos $$13 = (BlockPos)var12.next();
         if ($$12 > 0) {
            $$12--;
            placeSuspiciousSand($$4, $$0, $$13);
         } else if ($$4.isInside($$13)) {
            $$0.setBlock($$13, Blocks.SAND.defaultBlockState(), 2);
         }
      }
   }

   private static void placeSuspiciousSand(BoundingBox $$0, net.minecraft.world.level.WorldGenLevel $$1, BlockPos $$2) {
      if ($$0.isInside($$2)) {
         $$1.setBlock($$2, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 2);
         $$1.getBlockEntity($$2, BlockEntityType.BRUSHABLE_BLOCK)
            .ifPresent($$1x -> $$1x.setLootTable(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY, $$2.asLong()));
      }
   }

   @Override
   public StructureType<?> type() {
      return StructureType.DESERT_PYRAMID;
   }
}
