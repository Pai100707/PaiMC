/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class ParallelMapTransform {
    private static final int DEFAULT_TASKS_PER_THREAD = 16;

    public static <K, U, V> CompletableFuture<Map<K, V>> schedule(Map<K, U> $$0, BiFunction<K, U, @Nullable V> $$1, int $$2, Executor $$3) {
        int $$4 = $$0.size();
        if ($$4 == 0) {
            return CompletableFuture.completedFuture(Map.of());
        }
        if ($$4 == 1) {
            Map.Entry<K, U> $$5 = $$0.entrySet().iterator().next();
            Object $$6 = $$5.getKey();
            Object $$7 = $$5.getValue();
            return CompletableFuture.supplyAsync(() -> {
                Object $$3 = $$1.apply($$6, $$7);
                return $$3 != null ? Map.of($$6, $$3) : Map.of();
            }, $$3);
        }
        SplitterBase $$8 = $$4 <= $$2 ? new SingleTaskSplitter<K, U, V>($$1, $$4) : new BatchedTaskSplitter<K, U, V>($$1, $$4, $$2);
        return $$8.scheduleTasks($$0, $$3);
    }

    public static <K, U, V> CompletableFuture<Map<K, V>> schedule(Map<K, U> $$0, BiFunction<K, U, @Nullable V> $$1, Executor $$2) {
        int $$3 = Util.maxAllowedExecutorThreads() * 16;
        return ParallelMapTransform.schedule($$0, $$1, $$3, $$2);
    }

    static class SingleTaskSplitter<K, U, V>
    extends SplitterBase<K, U, V> {
        SingleTaskSplitter(BiFunction<K, U, V> $$0, int $$1) {
            super($$0, $$1, $$1);
        }

        @Override
        protected int batchSize(int $$0) {
            return 1;
        }

        @Override
        protected CompletableFuture<?> scheduleBatch(Container<K, U, V> $$0, int $$1, int $$2, Executor $$3) {
            assert ($$1 + 1 == $$2);
            return CompletableFuture.runAsync(() -> $$0.applyOperation($$1), $$3);
        }

        @Override
        protected CompletableFuture<Map<K, V>> scheduleFinalOperation(CompletableFuture<?> $$0, Container<K, U, V> $$12) {
            return $$0.thenApply($$1 -> {
                HashMap $$2 = new HashMap($$12.size());
                for (int $$3 = 0; $$3 < $$12.size(); ++$$3) {
                    $$12.copyOut($$3, $$2);
                }
                return $$2;
            });
        }
    }

    static class BatchedTaskSplitter<K, U, V>
    extends SplitterBase<K, U, V> {
        private final Map<K, V> result;
        private final int batchSize;
        private final int firstUndersizedBatchIndex;

        BatchedTaskSplitter(BiFunction<K, U, V> $$0, int $$1, int $$2) {
            super($$0, $$1, $$2);
            this.result = new HashMap($$1);
            this.batchSize = Mth.positiveCeilDiv($$1, $$2);
            int $$3 = this.batchSize * $$2;
            int $$4 = $$3 - $$1;
            this.firstUndersizedBatchIndex = $$2 - $$4;
            assert (this.firstUndersizedBatchIndex > 0 && this.firstUndersizedBatchIndex <= $$2);
        }

        @Override
        protected CompletableFuture<?> scheduleBatch(Container<K, U, V> $$0, int $$1, int $$2, Executor $$3) {
            int $$4 = $$2 - $$1;
            assert ($$4 == this.batchSize || $$4 == this.batchSize - 1);
            return CompletableFuture.runAsync(BatchedTaskSplitter.createTask(this.result, $$1, $$2, $$0), $$3);
        }

        @Override
        protected int batchSize(int $$0) {
            return $$0 < this.firstUndersizedBatchIndex ? this.batchSize : this.batchSize - 1;
        }

        private static <K, U, V> Runnable createTask(Map<K, V> $$0, int $$1, int $$2, Container<K, U, V> $$3) {
            return () -> {
                for (int $$4 = $$1; $$4 < $$2; ++$$4) {
                    $$3.applyOperation($$4);
                }
                Map map = $$0;
                synchronized (map) {
                    for (int $$5 = $$1; $$5 < $$2; ++$$5) {
                        $$3.copyOut($$5, $$0);
                    }
                }
            };
        }

        @Override
        protected CompletableFuture<Map<K, V>> scheduleFinalOperation(CompletableFuture<?> $$0, Container<K, U, V> $$12) {
            Map $$2 = this.result;
            return $$0.thenApply($$1 -> $$2);
        }
    }

    static abstract class SplitterBase<K, U, V> {
        private int lastScheduledIndex;
        private int currentIndex;
        private final CompletableFuture<?>[] tasks;
        private int batchIndex;
        private final Container<K, U, V> container;

        SplitterBase(BiFunction<K, U, V> $$0, int $$1, int $$2) {
            this.container = new Container<K, U, V>($$0, $$1);
            this.tasks = new CompletableFuture[$$2];
        }

        private int pendingBatchSize() {
            return this.currentIndex - this.lastScheduledIndex;
        }

        public CompletableFuture<Map<K, V>> scheduleTasks(Map<K, U> $$0, Executor $$12) {
            $$0.forEach(($$1, $$2) -> {
                this.container.put(this.currentIndex++, $$1, $$2);
                if (this.pendingBatchSize() == this.batchSize(this.batchIndex)) {
                    this.tasks[this.batchIndex++] = this.scheduleBatch(this.container, this.lastScheduledIndex, this.currentIndex, $$12);
                    this.lastScheduledIndex = this.currentIndex;
                }
            });
            assert (this.currentIndex == this.container.size());
            assert (this.lastScheduledIndex == this.currentIndex);
            assert (this.batchIndex == this.tasks.length);
            return this.scheduleFinalOperation(CompletableFuture.allOf(this.tasks), this.container);
        }

        protected abstract int batchSize(int var1);

        protected abstract CompletableFuture<?> scheduleBatch(Container<K, U, V> var1, int var2, int var3, Executor var4);

        protected abstract CompletableFuture<Map<K, V>> scheduleFinalOperation(CompletableFuture<?> var1, Container<K, U, V> var2);
    }

    record Container<K, U, V>(BiFunction<K, U, V> operation, @Nullable Object[] keys, @Nullable Object[] values) {
        public Container(BiFunction<K, U, V> $$0, int $$1) {
            this($$0, new Object[$$1], new Object[$$1]);
        }

        public void put(int $$0, K $$1, U $$2) {
            this.keys[$$0] = $$1;
            this.values[$$0] = $$2;
        }

        private @Nullable K key(int $$0) {
            return (K)this.keys[$$0];
        }

        private @Nullable V output(int $$0) {
            return (V)this.values[$$0];
        }

        private @Nullable U input(int $$0) {
            return (U)this.values[$$0];
        }

        public void applyOperation(int $$0) {
            this.values[$$0] = this.operation.apply(this.key($$0), this.input($$0));
        }

        public void copyOut(int $$0, Map<K, V> $$1) {
            V $$2 = this.output($$0);
            if ($$2 != null) {
                K $$3 = this.key($$0);
                $$1.put($$3, $$2);
            }
        }

        public int size() {
            return this.keys.length;
        }
    }
}

