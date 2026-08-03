package net.minecraft.server.level;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;

public abstract class ChunkTracker extends DynamicGraphMinFixedPoint {
   protected ChunkTracker(int $$0, int $$1, int $$2) {
      super($$0, $$1, $$2);
   }

   protected boolean isSource(long $$0) {
      return $$0 == ChunkPos.INVALID_CHUNK_POS;
   }

   protected void checkNeighborsAfterUpdate(long $$0, int $$1, boolean $$2) {
      if (!$$2 || $$1 < this.levelCount - 2) {
         ChunkPos $$3 = new ChunkPos($$0);
         int $$4 = $$3.x;
         int $$5 = $$3.z;

         for (int $$6 = -1; $$6 <= 1; $$6++) {
            for (int $$7 = -1; $$7 <= 1; $$7++) {
               long $$8 = ChunkPos.asLong($$4 + $$6, $$5 + $$7);
               if ($$8 != $$0) {
                  this.checkNeighbor($$0, $$8, $$1, $$2);
               }
            }
         }
      }
   }

   protected int getComputedLevel(long $$0, long $$1, int $$2) {
      int $$3 = $$2;
      ChunkPos $$4 = new ChunkPos($$0);
      int $$5 = $$4.x;
      int $$6 = $$4.z;

      for (int $$7 = -1; $$7 <= 1; $$7++) {
         for (int $$8 = -1; $$8 <= 1; $$8++) {
            long $$9 = ChunkPos.asLong($$5 + $$7, $$6 + $$8);
            if ($$9 == $$0) {
               $$9 = ChunkPos.INVALID_CHUNK_POS;
            }

            if ($$9 != $$1) {
               int $$10 = this.computeLevelFromNeighbor($$9, $$0, this.getLevel($$9));
               if ($$3 > $$10) {
                  $$3 = $$10;
               }

               if ($$3 == 0) {
                  return $$3;
               }
            }
         }
      }

      return $$3;
   }

   protected int computeLevelFromNeighbor(long $$0, long $$1, int $$2) {
      return $$0 == ChunkPos.INVALID_CHUNK_POS ? this.getLevelFromSource($$1) : $$2 + 1;
   }

   protected abstract int getLevelFromSource(long var1);

   public void update(long $$0, int $$1, boolean $$2) {
      this.checkEdge(ChunkPos.INVALID_CHUNK_POS, $$0, $$1, $$2);
   }
}
