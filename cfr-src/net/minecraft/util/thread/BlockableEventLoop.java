/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Queues
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  javax.annotation.CheckReturnValue
 *  org.slf4j.Logger
 */
package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.CheckReturnValue;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.ProfilerMeasured;
import net.minecraft.util.thread.TaskScheduler;
import org.slf4j.Logger;

public abstract class BlockableEventLoop<R extends Runnable>
implements ProfilerMeasured,
TaskScheduler<R>,
Executor {
    public static final long BLOCK_TIME_NANOS = 100000L;
    private final String name;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Queue<R> pendingRunnables = Queues.newConcurrentLinkedQueue();
    private int blockingCount;

    protected BlockableEventLoop(String $$0) {
        this.name = $$0;
        MetricsRegistry.INSTANCE.add(this);
    }

    protected abstract boolean shouldRun(R var1);

    public boolean isSameThread() {
        return Thread.currentThread() == this.getRunningThread();
    }

    protected abstract Thread getRunningThread();

    protected boolean scheduleExecutables() {
        return !this.isSameThread();
    }

    public int getPendingTasksCount() {
        return this.pendingRunnables.size();
    }

    @Override
    public String name() {
        return this.name;
    }

    public <V> CompletableFuture<V> submit(Supplier<V> $$0) {
        if (this.scheduleExecutables()) {
            return CompletableFuture.supplyAsync($$0, this);
        }
        return CompletableFuture.completedFuture($$0.get());
    }

    private CompletableFuture<Void> submitAsync(Runnable $$0) {
        return CompletableFuture.supplyAsync(() -> {
            $$0.run();
            return null;
        }, this);
    }

    @CheckReturnValue
    public CompletableFuture<Void> submit(Runnable $$0) {
        if (this.scheduleExecutables()) {
            return this.submitAsync($$0);
        }
        $$0.run();
        return CompletableFuture.completedFuture(null);
    }

    public void executeBlocking(Runnable $$0) {
        if (!this.isSameThread()) {
            this.submitAsync($$0).join();
        } else {
            $$0.run();
        }
    }

    @Override
    public void schedule(R $$0) {
        this.pendingRunnables.add($$0);
        LockSupport.unpark(this.getRunningThread());
    }

    @Override
    public void execute(Runnable $$0) {
        Object $$1 = this.wrapRunnable($$0);
        if (this.scheduleExecutables()) {
            this.schedule($$1);
        } else {
            this.doRunTask($$1);
        }
    }

    public void executeIfPossible(Runnable $$0) {
        this.execute($$0);
    }

    protected void dropAllTasks() {
        this.pendingRunnables.clear();
    }

    protected void runAllTasks() {
        while (this.pollTask()) {
        }
    }

    protected boolean shouldRunAllTasks() {
        return this.blockingCount > 0;
    }

    public boolean pollTask() {
        Runnable $$0 = (Runnable)this.pendingRunnables.peek();
        if ($$0 == null) {
            return false;
        }
        if (!this.shouldRunAllTasks() && !this.shouldRun($$0)) {
            return false;
        }
        this.doRunTask((Runnable)this.pendingRunnables.remove());
        return true;
    }

    public void managedBlock(BooleanSupplier $$0) {
        ++this.blockingCount;
        try {
            while (!$$0.getAsBoolean()) {
                if (this.pollTask()) continue;
                this.waitForTasks();
            }
        }
        finally {
            --this.blockingCount;
        }
    }

    protected void waitForTasks() {
        Thread.yield();
        LockSupport.parkNanos("waiting for tasks", 100000L);
    }

    protected void doRunTask(R $$0) {
        block8: {
            try (Zone $$1 = TracyClient.beginZone((String)"Task", (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                $$0.run();
            }
            catch (Exception $$2) {
                LOGGER.error(LogUtils.FATAL_MARKER, "Error executing task on {}", (Object)this.name(), (Object)$$2);
                if (!BlockableEventLoop.isNonRecoverable($$2)) break block8;
                throw $$2;
            }
        }
    }

    @Override
    public List<MetricSampler> profiledMetrics() {
        return ImmutableList.of((Object)MetricSampler.create(this.name + "-pending-tasks", MetricCategory.EVENT_LOOPS, this::getPendingTasksCount));
    }

    public static boolean isNonRecoverable(Throwable $$0) {
        if ($$0 instanceof ReportedException) {
            ReportedException $$1 = (ReportedException)$$0;
            return BlockableEventLoop.isNonRecoverable($$1.getCause());
        }
        return $$0 instanceof OutOfMemoryError || $$0 instanceof StackOverflowError;
    }
}

