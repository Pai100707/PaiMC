/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;

public interface StrictQueue<T extends Runnable> {
    public @Nullable Runnable pop();

    public boolean push(T var1);

    public boolean isEmpty();

    public int size();

    public static final class FixedPriorityQueue
    implements StrictQueue<RunnableWithPriority> {
        private final Queue<Runnable>[] queues;
        private final AtomicInteger size = new AtomicInteger();

        public FixedPriorityQueue(int $$0) {
            this.queues = new Queue[$$0];
            for (int $$1 = 0; $$1 < $$0; ++$$1) {
                this.queues[$$1] = Queues.newConcurrentLinkedQueue();
            }
        }

        @Override
        public @Nullable Runnable pop() {
            for (Queue<Runnable> $$0 : this.queues) {
                Runnable $$1 = $$0.poll();
                if ($$1 == null) continue;
                this.size.decrementAndGet();
                return $$1;
            }
            return null;
        }

        @Override
        public boolean push(RunnableWithPriority $$0) {
            int $$1 = $$0.priority;
            if ($$1 >= this.queues.length || $$1 < 0) {
                throw new IndexOutOfBoundsException(String.format(Locale.ROOT, "Priority %d not supported. Expected range [0-%d]", $$1, this.queues.length - 1));
            }
            this.queues[$$1].add($$0);
            this.size.incrementAndGet();
            return true;
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

    public static final class RunnableWithPriority
    extends Record
    implements Runnable {
        final int priority;
        private final Runnable task;

        public RunnableWithPriority(int $$0, Runnable $$1) {
            this.priority = $$0;
            this.task = $$1;
        }

        @Override
        public void run() {
            this.task.run();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RunnableWithPriority.class, "priority;task", "priority", "task"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RunnableWithPriority.class, "priority;task", "priority", "task"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RunnableWithPriority.class, "priority;task", "priority", "task"}, this, $$0);
        }

        public int priority() {
            return this.priority;
        }

        public Runnable task() {
            return this.task;
        }
    }

    public static final class QueueStrictQueue
    implements StrictQueue<Runnable> {
        private final Queue<Runnable> queue;

        public QueueStrictQueue(Queue<Runnable> $$0) {
            this.queue = $$0;
        }

        @Override
        public @Nullable Runnable pop() {
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
}

