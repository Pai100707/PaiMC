package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChunkSkyLightSources {
   private static final int SIZE = 16;
   public static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
   private final int minY;
   private final BitStorage heightmap;
   private final MutableBlockPos mutablePos1 = new MutableBlockPos();
   private final MutableBlockPos mutablePos2 = new MutableBlockPos();

   public ChunkSkyLightSources(net.minecraft.world.level.LevelHeightAccessor $$0) {
      this.minY = $$0.getMinY() - 1;
      int $$1 = $$0.getMaxY() + 1;
      int $$2 = Mth.ceillog2($$1 - this.minY + 1);
      this.heightmap = new SimpleBitStorage($$2, 256);
   }

   public void fillFrom(ChunkAccess $$0) {
      int $$1 = $$0.getHighestFilledSectionIndex();
      if ($$1 == -1) {
         this.fill(this.minY);
      } else {
         for (int $$2 = 0; $$2 < 16; $$2++) {
            for (int $$3 = 0; $$3 < 16; $$3++) {
               int $$4 = Math.max(this.findLowestSourceY($$0, $$1, $$3, $$2), this.minY);
               this.set(index($$3, $$2), $$4);
            }
         }
      }
   }

   private int findLowestSourceY(ChunkAccess $$0, int $$1, int $$2, int $$3) {
      int $$4 = SectionPos.sectionToBlockCoord($$0.getSectionYFromSectionIndex($$1) + 1);
      MutableBlockPos $$5 = this.mutablePos1.set($$2, $$4, $$3);
      MutableBlockPos $$6 = this.mutablePos2.setWithOffset($$5, Direction.DOWN);
      BlockState $$7 = Blocks.AIR.defaultBlockState();

      for (int $$8 = $$1; $$8 >= 0; $$8--) {
         LevelChunkSection $$9 = $$0.getSection($$8);
         if ($$9.hasOnlyAir()) {
            $$7 = Blocks.AIR.defaultBlockState();
            int $$10 = $$0.getSectionYFromSectionIndex($$8);
            $$5.setY(SectionPos.sectionToBlockCoord($$10));
            $$6.setY($$5.getY() - 1);
         } else {
            for (int $$11 = 15; $$11 >= 0; $$11--) {
               BlockState $$12 = $$9.getBlockState($$2, $$11, $$3);
               if (isEdgeOccluded($$7, $$12)) {
                  return $$5.getY();
               }

               $$7 = $$12;
               $$5.set($$6);
               $$6.move(Direction.DOWN);
            }
         }
      }

      return this.minY;
   }

   public boolean update(net.minecraft.world.level.BlockGetter $$0, int $$1, int $$2, int $$3) {
      int $$4 = $$2 + 1;
      int $$5 = index($$1, $$3);
      int $$6 = this.get($$5);
      if ($$4 < $$6) {
         return false;
      } else {
         BlockPos $$7 = this.mutablePos1.set($$1, $$2 + 1, $$3);
         BlockState $$8 = $$0.getBlockState($$7);
         BlockPos $$9 = this.mutablePos2.set($$1, $$2, $$3);
         BlockState $$10 = $$0.getBlockState($$9);
         if (this.updateEdge($$0, $$5, $$6, $$7, $$8, $$9, $$10)) {
            return true;
         } else {
            BlockPos $$11 = this.mutablePos1.set($$1, $$2 - 1, $$3);
            BlockState $$12 = $$0.getBlockState($$11);
            return this.updateEdge($$0, $$5, $$6, $$9, $$10, $$11, $$12);
         }
      }
   }

   private boolean updateEdge(net.minecraft.world.level.BlockGetter $$0, int $$1, int $$2, BlockPos $$3, BlockState $$4, BlockPos $$5, BlockState $$6) {
      int $$7 = $$3.getY();
      if (isEdgeOccluded($$4, $$6)) {
         if ($$7 > $$2) {
            this.set($$1, $$7);
            return true;
         }
      } else if ($$7 == $$2) {
         this.set($$1, this.findLowestSourceBelow($$0, $$5, $$6));
         return true;
      }

      return false;
   }

   private int findLowestSourceBelow(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2) {
      MutableBlockPos $$3 = this.mutablePos1.set($$1);
      MutableBlockPos $$4 = this.mutablePos2.setWithOffset($$1, Direction.DOWN);
      BlockState $$5 = $$2;

      while ($$4.getY() >= this.minY) {
         BlockState $$6 = $$0.getBlockState($$4);
         if (isEdgeOccluded($$5, $$6)) {
            return $$3.getY();
         }

         $$5 = $$6;
         $$3.set($$4);
         $$4.move(Direction.DOWN);
      }

      return this.minY;
   }

   private static boolean isEdgeOccluded(BlockState $$0, BlockState $$1) {
      if ($$1.getLightBlock() != 0) {
         return true;
      } else {
         VoxelShape $$2 = LightEngine.getOcclusionShape($$0, Direction.DOWN);
         VoxelShape $$3 = LightEngine.getOcclusionShape($$1, Direction.UP);
         return Shapes.faceShapeOccludes($$2, $$3);
      }
   }

   public int getLowestSourceY(int $$0, int $$1) {
      int $$2 = this.get(index($$0, $$1));
      return this.extendSourcesBelowWorld($$2);
   }

   public int getHighestLowestSourceY() {
      int $$0 = Integer.MIN_VALUE;

      for (int $$1 = 0; $$1 < this.heightmap.getSize(); $$1++) {
         int $$2 = this.heightmap.get($$1);
         if ($$2 > $$0) {
            $$0 = $$2;
         }
      }

      return this.extendSourcesBelowWorld($$0 + this.minY);
   }

   private void fill(int $$0) {
      int $$1 = $$0 - this.minY;

      for (int $$2 = 0; $$2 < this.heightmap.getSize(); $$2++) {
         this.heightmap.set($$2, $$1);
      }
   }

   private void set(int $$0, int $$1) {
      this.heightmap.set($$0, $$1 - this.minY);
   }

   private int get(int $$0) {
      return this.heightmap.get($$0) + this.minY;
   }

   private int extendSourcesBelowWorld(int $$0) {
      return $$0 == this.minY ? Integer.MIN_VALUE : $$0;
   }

   private static int index(int $$0, int $$1) {
      return $$0 + $$1 * 16;
   }
}
