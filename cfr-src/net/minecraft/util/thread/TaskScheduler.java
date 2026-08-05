/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public interface TaskScheduler<R extends Runnable>
extends AutoCloseable {
    public String name();

    public void schedule(R var1);

    @Override
    default public void close() {
    }

    public R wrapRunnable(Runnable var1);

    default public <Source> CompletableFuture<Source> scheduleWithResult(Consumer<CompletableFuture<Source>> $$0) {
        CompletableFuture $$1 = new CompletableFuture();
        this.schedule(this.wrapRunnable(() -> $$0.accept($$1)));
        return $$1;
    }

    public static TaskScheduler<Runnable> wrapExecutor(final String $$0, final Executor $$1) {
        return new TaskScheduler<Runnable>(){

            @Override
            public String name() {
                return $$0;
            }

            @Override
            public void schedule(Runnable $$02) {
                $$1.execute($$02);
            }

            @Override
            public Runnable wrapRunnable(Runnable $$02) {
                return $$02;
            }

            public String toString() {
                return $$0;
            }
        };
    }
}

