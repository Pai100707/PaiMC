package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;

public class StructurePiecesBuilder implements StructurePieceAccessor {
   private final List<StructurePiece> pieces = Lists.newArrayList();

   @Override
   public void addPiece(StructurePiece $$0) {
      this.pieces.add($$0);
   }

   
   @Override
   public StructurePiece findCollisionPiece(BoundingBox $$0) {
      return StructurePiece.findCollisionPiece(this.pieces, $$0);
   }

   @Deprecated
   public void offsetPiecesVertically(int $$0) {
      for (StructurePiece $$1 : this.pieces) {
         $$1.move(0, $$0, 0);
      }
   }

   @Deprecated
   public int moveBelowSeaLevel(int $$0, int $$1, RandomSource $$2, int $$3) {
      int $$4 = $$0 - $$3;
      BoundingBox $$5 = this.getBoundingBox();
      int $$6 = $$5.getYSpan() + $$1 + 1;
      if ($$6 < $$4) {
         $$6 += $$2.nextInt($$4 - $$6);
      }

      int $$7 = $$6 - $$5.maxY();
      this.offsetPiecesVertically($$7);
      return $$7;
   }

   /** @deprecated */
   public void moveInsideHeights(RandomSource $$0, int $$1, int $$2) {
      BoundingBox $$3 = this.getBoundingBox();
      int $$4 = $$2 - $$1 + 1 - $$3.getYSpan();
      int $$5;
      if ($$4 > 1) {
         $$5 = $$1 + $$0.nextInt($$4);
      } else {
         $$5 = $$1;
      }

      int $$7 = $$5 - $$3.minY();
      this.offsetPiecesVertically($$7);
   }

   public PiecesContainer build() {
      return new PiecesContainer(this.pieces);
   }

   public void clear() {
      this.pieces.clear();
   }

   public boolean isEmpty() {
      return this.pieces.isEmpty();
   }

   public BoundingBox getBoundingBox() {
      return StructurePiece.createBoundingBox(this.pieces.stream());
   }
}
