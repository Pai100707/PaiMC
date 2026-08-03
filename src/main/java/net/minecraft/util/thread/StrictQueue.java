package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;

public interface StrictQueue<T extends Runnable> {
   @Nullable
   Runnable pop();

   boolean push(T var1);

   boolean isEmpty();

   int size();

   public static final class FixedPriorityQueue implements StrictQueue<StrictQueue.RunnableWithPriority> {
      private final Queue<Runnable>[] queues;
      private final AtomicInteger size = new AtomicInteger();

      public FixedPriorityQueue(int $$0) {
         this.queues = new Queue[$$0];

         for (int $$1 = 0; $$1 < $$0; $$1++) {
            this.queues[$$1] = Queues.newConcurrentLinkedQueue();
         }
      }

      @Nullable
      @Override
      public Runnable pop() {
         for (Queue<Runnable> $$0 : this.queues) {
            Runnable $$1 = $$0.poll();
            if ($$1 != null) {
               this.size.decrementAndGet();
               return $$1;
            }
         }

         return null;
      }

      public boolean push(StrictQueue.RunnableWithPriority $$0) {
         int $$1 = $$0.priority;
         if ($$1 < this.queues.length && $$1 >= 0) {
            this.queues[$$1].add($$0);
            this.size.incrementAndGet();
            return true;
         } else {
            throw new IndexOutOfBoundsException(String.format(Locale.ROOT, "Priority %d not supported. Expected range [0-%d]", $$1, this.queues.length - 1));
         }
      }

      @Override
      public boolean isEmpty() {
         return this.size.get() == 0;
      }

      @Override
      public int size() {
         return this.size.get();
      }
   }

   public static final class QueueStrictQueue implements StrictQueue<Runnable> {
      private final Queue<Runnable> queue;

      public QueueStrictQueue(Queue<Runnable> $$0) {
         this.queue = $$0;
      }

      @Nullable
      @Override
      public Runnable pop() {
         return this.queue.poll();
      }

      @Override
      public boolean push(Runnable $$0) {
         return this.queue.add($$0);
      }

      @Override
      public boolean isEmpty() {
         return this.queue.isEmpty();
      }

      @Override
      public int size() {
         return this.queue.size();
      }
   }

   public record RunnableWithPriority(int priority, Runnable task) implements Runnable {

      @Override
      public void run() {
         this.task.run();
      }
   }
}
