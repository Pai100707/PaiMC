package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Deque;
import org.jspecify.annotations.Nullable;

public final class SequencedPriorityIterator<T> extends AbstractIterator<T> {
   private static final int MIN_PRIO = Integer.MIN_VALUE;
   @Nullable
   private Deque<T> highestPrioQueue = null;
   private int highestPrio = Integer.MIN_VALUE;
   private final Int2ObjectMap<Deque<T>> queuesByPriority = new Int2ObjectOpenHashMap();

   public void add(T $$0, int $$1) {
      if ($$1 == this.highestPrio && this.highestPrioQueue != null) {
         this.highestPrioQueue.addLast($$0);
      } else {
         Deque<T> $$2 = (Deque<T>)this.queuesByPriority.computeIfAbsent($$1, $$0x -> Queues.newArrayDeque());
         $$2.addLast($$0);
         if ($$1 >= this.highestPrio) {
            this.highestPrioQueue = $$2;
            this.highestPrio = $$1;
         }
      }
   }

   @Nullable
   protected T computeNext() {
      if (this.highestPrioQueue == null) {
         return (T)this.endOfData();
      } else {
         T $$0 = this.highestPrioQueue.removeFirst();
         if ($$0 == null) {
            return (T)this.endOfData();
         } else {
            if (this.highestPrioQueue.isEmpty()) {
               this.switchCacheToNextHighestPrioQueue();
            }

            return $$0;
         }
      }
   }

   private void switchCacheToNextHighestPrioQueue() {
      int $$0 = Integer.MIN_VALUE;
      Deque<T> $$1 = null;
      ObjectIterator var3 = Int2ObjectMaps.fastIterable(this.queuesByPriority).iterator();

      while (var3.hasNext()) {
         Entry<Deque<T>> $$2 = (Entry<Deque<T>>)var3.next();
         Deque<T> $$3 = (Deque<T>)$$2.getValue();
         int $$4 = $$2.getIntKey();
         if ($$4 > $$0 && !$$3.isEmpty()) {
            $$0 = $$4;
            $$1 = $$3;
            if ($$4 == this.highestPrio - 1) {
               break;
            }
         }
      }

      this.highestPrio = $$0;
      this.highestPrioQueue = $$1;
   }
}
