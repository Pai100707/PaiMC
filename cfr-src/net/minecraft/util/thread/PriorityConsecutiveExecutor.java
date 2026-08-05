/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.thread.AbstractConsecutiveExecutor;
import net.minecraft.util.thread.StrictQueue;

public class PriorityConsecutiveExecutor
extends AbstractConsecutiveExecutor<StrictQueue.RunnableWithPriority> {
    public PriorityConsecutiveExecutor(int $$0, Executor $$1, String $$2) {
        super(new StrictQueue.FixedPriorityQueue($$0), $$1, $$2);
        MetricsRegistry.INSTANCE.add(this);
    }

    @Override
    public StrictQueue.RunnableWithPriority wrapRunnable(Runnable $$0) {
        return new StrictQueue.RunnableWithPriority(0, $$0);
    }

    public <Source> CompletableFuture<Source> scheduleWithResult(int $$0, Consumer<CompletableFuture<Source>> $$1) {
        CompletableFuture $$2 = new CompletableFuture();
        this.schedule(new StrictQueue.RunnableWithPriority($$0, () -> $$1.accept($$2)));
        return $$2;
    }

    @Override
    public /* synthetic */ Runnable wrapRunnable(Runnable runnable) {
        return this.wrapRunnable(runnable);
    }
}

