package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public abstract class ScatteredFeaturePiece extends StructurePiece {
   protected final int width;
   protected final int height;
   protected final int depth;
   protected int heightPosition = -1;

   protected ScatteredFeaturePiece(StructurePieceType $$0, int $$1, int $$2, int $$3, int $$4, int $$5, int $$6, Direction $$7) {
      super($$0, 0, StructurePiece.makeBoundingBox($$1, $$2, $$3, $$7, $$4, $$5, $$6));
      this.width = $$4;
      this.height = $$5;
      this.depth = $$6;
      this.setOrientation($$7);
   }

   protected ScatteredFeaturePiece(StructurePieceType $$0, CompoundTag $$1) {
      super($$0, $$1);
      this.width = $$1.getIntOr("Width", 0);
      this.height = $$1.getIntOr("Height", 0);
      this.depth = $$1.getIntOr("Depth", 0);
      this.heightPosition = $$1.getIntOr("HPos", 0);
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
      $$1.putInt("Width", this.width);
      $$1.putInt("Height", this.height);
      $$1.putInt("Depth", this.depth);
      $$1.putInt("HPos", this.heightPosition);
   }

   protected boolean updateAverageGroundHeight(net.minecraft.world.level.LevelAccessor $$0, BoundingBox $$1, int $$2) {
      if (this.heightPosition >= 0) {
         return true;
      } else {
         int $$3 = 0;
         int $$4 = 0;
         MutableBlockPos $$5 = new MutableBlockPos();

         for (int $$6 = this.boundingBox.minZ(); $$6 <= this.boundingBox.maxZ(); $$6++) {
            for (int $$7 = this.boundingBox.minX(); $$7 <= this.boundingBox.maxX(); $$7++) {
               $$5.set($$7, 64, $$6);
               if ($$1.isInside($$5)) {
                  $$3 += $$0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, $$5).getY();
                  $$4++;
               }
            }
         }

         if ($$4 == 0) {
            return false;
         } else {
            this.heightPosition = $$3 / $$4;
            this.boundingBox.move(0, this.heightPosition - this.boundingBox.minY() + $$2, 0);
            return true;
         }
      }
   }

   protected boolean updateHeightPositionToLowestGroundHeight(net.minecraft.world.level.LevelAccessor $$0, int $$1) {
      if (this.heightPosition >= 0) {
         return true;
      } else {
         int $$2 = $$0.getMaxY() + 1;
         boolean $$3 = false;
         MutableBlockPos $$4 = new MutableBlockPos();

         for (int $$5 = this.boundingBox.minZ(); $$5 <= this.boundingBox.maxZ(); $$5++) {
            for (int $$6 = this.boundingBox.minX(); $$6 <= this.boundingBox.maxX(); $$6++) {
               $$4.set($$6, 0, $$5);
               $$2 = Math.min($$2, $$0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, $$4).getY());
               $$3 = true;
            }
         }

         if (!$$3) {
            return false;
         } else {
            this.heightPosition = $$2;
            this.boundingBox.move(0, this.heightPosition - this.boundingBox.minY() + $$1, 0);
            return true;
         }
      }
   }
}
