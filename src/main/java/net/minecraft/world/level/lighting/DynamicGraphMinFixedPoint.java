package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.function.LongPredicate;
import net.minecraft.util.Mth;

public abstract class DynamicGraphMinFixedPoint {
   public static final long SOURCE = Long.MAX_VALUE;
   private static final int NO_COMPUTED_LEVEL = 255;
   protected final int levelCount;
   private final LeveledPriorityQueue priorityQueue;
   private final Long2ByteMap computedLevels;
   private volatile boolean hasWork;

   protected DynamicGraphMinFixedPoint(int $$0, int $$1, final int $$2) {
      if ($$0 >= 254) {
         throw new IllegalArgumentException("Level count must be < 254.");
      } else {
         this.levelCount = $$0;
         this.priorityQueue = new LeveledPriorityQueue($$0, $$1);
         this.computedLevels = new Long2ByteOpenHashMap($$2, 0.5F) {
            protected void rehash(int $$0) {
               if ($$0 > $$2) {
                  super.rehash($$0);
               }
            }
         };
         this.computedLevels.defaultReturnValue((byte)-1);
      }
   }

   protected void removeFromQueue(long $$0) {
      int $$1 = this.computedLevels.remove($$0) & 255;
      if ($$1 != 255) {
         int $$2 = this.getLevel($$0);
         int $$3 = this.calculatePriority($$2, $$1);
         this.priorityQueue.dequeue($$0, $$3, this.levelCount);
         this.hasWork = !this.priorityQueue.isEmpty();
      }
   }

   public void removeIf(LongPredicate $$0) {
      LongList $$1 = new LongArrayList();
      this.computedLevels.keySet().forEach($$2 -> {
         if ($$0.test($$2)) {
            $$1.add($$2);
         }
      });
      $$1.forEach(this::removeFromQueue);
   }

   private int calculatePriority(int $$0, int $$1) {
      return Math.min(Math.min($$0, $$1), this.levelCount - 1);
   }

   protected void checkNode(long $$0) {
      this.checkEdge($$0, $$0, this.levelCount - 1, false);
   }

   protected void checkEdge(long $$0, long $$1, int $$2, boolean $$3) {
      this.checkEdge($$0, $$1, $$2, this.getLevel($$1), this.computedLevels.get($$1) & 255, $$3);
      this.hasWork = !this.priorityQueue.isEmpty();
   }

   private void checkEdge(long $$0, long $$1, int $$2, int $$3, int $$4, boolean $$5) {
      if (!this.isSource($$1)) {
         $$2 = Mth.clamp($$2, 0, this.levelCount - 1);
         $$3 = Mth.clamp($$3, 0, this.levelCount - 1);
         boolean $$6 = $$4 == 255;
         if ($$6) {
            $$4 = $$3;
         }

         int $$7;
         if ($$5) {
            $$7 = Math.min($$4, $$2);
         } else {
            $$7 = Mth.clamp(this.getComputedLevel($$1, $$0, $$2), 0, this.levelCount - 1);
         }

         int $$9 = this.calculatePriority($$3, $$4);
         if ($$3 != $$7) {
            int $$10 = this.calculatePriority($$3, $$7);
            if ($$9 != $$10 && !$$6) {
               this.priorityQueue.dequeue($$1, $$9, $$10);
            }

            this.priorityQueue.enqueue($$1, $$10);
            this.computedLevels.put($$1, (byte)$$7);
         } else if (!$$6) {
            this.priorityQueue.dequeue($$1, $$9, this.levelCount);
            this.computedLevels.remove($$1);
         }
      }
   }

   protected final void checkNeighbor(long $$0, long $$1, int $$2, boolean $$3) {
      int $$4 = this.computedLevels.get($$1) & 255;
      int $$5 = Mth.clamp(this.computeLevelFromNeighbor($$0, $$1, $$2), 0, this.levelCount - 1);
      if ($$3) {
         this.checkEdge($$0, $$1, $$5, this.getLevel($$1), $$4, $$3);
      } else {
         boolean $$6 = $$4 == 255;
         int $$7;
         if ($$6) {
            $$7 = Mth.clamp(this.getLevel($$1), 0, this.levelCount - 1);
         } else {
            $$7 = $$4;
         }

         if ($$5 == $$7) {
            this.checkEdge($$0, $$1, this.levelCount - 1, $$6 ? $$7 : this.getLevel($$1), $$4, $$3);
         }
      }
   }

   protected final boolean hasWork() {
      return this.hasWork;
   }

   protected final int runUpdates(int $$0) {
      if (this.priorityQueue.isEmpty()) {
         return $$0;
      } else {
         while (!this.priorityQueue.isEmpty() && $$0 > 0) {
            $$0--;
            long $$1 = this.priorityQueue.removeFirstLong();
            int $$2 = Mth.clamp(this.getLevel($$1), 0, this.levelCount - 1);
            int $$3 = this.computedLevels.remove($$1) & 255;
            if ($$3 < $$2) {
               this.setLevel($$1, $$3);
               this.checkNeighborsAfterUpdate($$1, $$3, true);
            } else if ($$3 > $$2) {
               this.setLevel($$1, this.levelCount - 1);
               if ($$3 != this.levelCount - 1) {
                  this.priorityQueue.enqueue($$1, this.calculatePriority(this.levelCount - 1, $$3));
                  this.computedLevels.put($$1, (byte)$$3);
               }

               this.checkNeighborsAfterUpdate($$1, $$2, false);
            }
         }

         this.hasWork = !this.priorityQueue.isEmpty();
         return $$0;
      }
   }

   public int getQueueSize() {
      return this.computedLevels.size();
   }

   protected boolean isSource(long $$0) {
      return $$0 == Long.MAX_VALUE;
   }

   protected abstract int getComputedLevel(long var1, long var3, int var5);

   protected abstract void checkNeighborsAfterUpdate(long var1, int var3, boolean var4);

   protected abstract int getLevel(long var1);

   protected abstract void setLevel(long var1, int var3);

   protected abstract int computeLevelFromNeighbor(long var1, long var3, int var5);
}
