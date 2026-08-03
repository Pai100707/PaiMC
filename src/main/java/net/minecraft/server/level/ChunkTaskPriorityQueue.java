package net.minecraft.server.level;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public class ChunkTaskPriorityQueue {
   public static final int PRIORITY_LEVEL_COUNT = ChunkLevel.MAX_LEVEL + 2;
   private final List<Long2ObjectLinkedOpenHashMap<List<Runnable>>> queuesPerPriority = IntStream.range(0, PRIORITY_LEVEL_COUNT)
      .mapToObj($$0x -> new Long2ObjectLinkedOpenHashMap())
      .toList();
   private volatile int topPriorityQueueIndex = PRIORITY_LEVEL_COUNT;
   private final String name;

   public ChunkTaskPriorityQueue(String $$0) {
      this.name = $$0;
   }

   protected void resortChunkTasks(int $$0, ChunkPos $$1, int $$2) {
      if ($$0 < PRIORITY_LEVEL_COUNT) {
         Long2ObjectLinkedOpenHashMap<List<Runnable>> $$3 = this.queuesPerPriority.get($$0);
         List<Runnable> $$4 = (List<Runnable>)$$3.remove($$1.toLong());
         if ($$0 == this.topPriorityQueueIndex) {
            while (this.hasWork() && this.queuesPerPriority.get(this.topPriorityQueueIndex).isEmpty()) {
               this.topPriorityQueueIndex++;
            }
         }

         if ($$4 != null && !$$4.isEmpty()) {
            ((List)this.queuesPerPriority.get($$2).computeIfAbsent($$1.toLong(), $$0x -> Lists.newArrayList())).addAll($$4);
            this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, $$2);
         }
      }
   }

   protected void submit(Runnable $$0, long $$1, int $$2) {
      ((List)this.queuesPerPriority.get($$2).computeIfAbsent($$1, $$0x -> Lists.newArrayList())).add($$0);
      this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, $$2);
   }

   protected void release(long $$0, boolean $$1) {
      for (Long2ObjectLinkedOpenHashMap<List<Runnable>> $$2 : this.queuesPerPriority) {
         List<Runnable> $$3 = (List<Runnable>)$$2.get($$0);
         if ($$3 != null) {
            if ($$1) {
               $$3.clear();
            }

            if ($$3.isEmpty()) {
               $$2.remove($$0);
            }
         }
      }

      while (this.hasWork() && this.queuesPerPriority.get(this.topPriorityQueueIndex).isEmpty()) {
         this.topPriorityQueueIndex++;
      }
   }

   @Nullable
   public ChunkTaskPriorityQueue.TasksForChunk pop() {
      if (!this.hasWork()) {
         return null;
      } else {
         int $$0 = this.topPriorityQueueIndex;
         Long2ObjectLinkedOpenHashMap<List<Runnable>> $$1 = this.queuesPerPriority.get($$0);
         long $$2 = $$1.firstLongKey();
         List<Runnable> $$3 = (List<Runnable>)$$1.removeFirst();

         while (this.hasWork() && this.queuesPerPriority.get(this.topPriorityQueueIndex).isEmpty()) {
            this.topPriorityQueueIndex++;
         }

         return new ChunkTaskPriorityQueue.TasksForChunk($$2, $$3);
      }
   }

   public boolean hasWork() {
      return this.topPriorityQueueIndex < PRIORITY_LEVEL_COUNT;
   }

   @Override
   public String toString() {
      return this.name + " " + this.topPriorityQueueIndex + "...";
   }

   public record TasksForChunk(long chunkPos, List<Runnable> tasks) {
   }
}
