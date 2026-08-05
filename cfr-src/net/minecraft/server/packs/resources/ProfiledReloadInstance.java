/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.resources;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import org.slf4j.Logger;

public class ProfiledReloadInstance
extends SimpleReloadInstance<State> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Stopwatch total = Stopwatch.createUnstarted();

    public static ReloadInstance of(ResourceManager $$0, List<PreparableReloadListener> $$12, Executor $$22, Executor $$32, CompletableFuture<Unit> $$42) {
        ProfiledReloadInstance $$5 = new ProfiledReloadInstance($$12);
        $$5.startTasks($$22, $$32, $$0, $$12, ($$1, $$2, $$3, $$4, $$52) -> {
            AtomicLong $$6 = new AtomicLong();
            AtomicLong $$7 = new AtomicLong();
            AtomicLong $$8 = new AtomicLong();
            AtomicLong $$9 = new AtomicLong();
            CompletableFuture<Void> $$10 = $$3.reload($$1, ProfiledReloadInstance.profiledExecutor($$4, $$6, $$7, $$3.getName()), $$2, ProfiledReloadInstance.profiledExecutor($$52, $$8, $$9, $$3.getName()));
            return $$10.thenApplyAsync($$5 -> {
                LOGGER.debug("Finished reloading {}", (Object)$$3.getName());
                return new State($$3.getName(), $$6, $$7, $$8, $$9);
            }, $$32);
        }, $$42);
        return $$5;
    }

    private ProfiledReloadInstance(List<PreparableReloadListener> $$0) {
        super($$0);
        this.total.start();
    }

    @Override
    protected CompletableFuture<List<State>> prepareTasks(Executor $$0, Executor $$1, ResourceManager $$2, List<PreparableReloadListener> $$3, SimpleReloadInstance.StateFactory<State> $$4, CompletableFuture<?> $$5) {
        return super.prepareTasks($$0, $$1, $$2, $$3, $$4, $$5).thenApplyAsync(this::finish, $$1);
    }

    private static Executor profiledExecutor(Executor $$0, AtomicLong $$1, AtomicLong $$2, String $$3) {
        return $$4 -> $$0.execute(() -> {
            Runnable $$4 = Profiler.get();
            $$4.push($$3);
            long $$5 = Util.getNanos();
            $$4.run();
            $$1.addAndGet(Util.getNanos() - $$5);
            $$2.incrementAndGet();
            $$4.pop();
        });
    }

    private List<State> finish(List<State> $$0) {
        this.total.stop();
        long $$1 = 0L;
        LOGGER.info("Resource reload finished after {} ms", (Object)this.total.elapsed(TimeUnit.MILLISECONDS));
        for (State $$2 : $$0) {
            long $$3 = TimeUnit.NANOSECONDS.toMillis($$2.preparationNanos.get());
            long $$4 = $$2.preparationCount.get();
            long $$5 = TimeUnit.NANOSECONDS.toMillis($$2.reloadNanos.get());
            long $$6 = $$2.reloadCount.get();
            long $$7 = $$3 + $$5;
            long $$8 = $$4 + $$6;
            String $$9 = $$2.name;
            LOGGER.info("{} took approximately {} tasks/{} ms ({} tasks/{} ms preparing, {} tasks/{} ms applying)", new Object[]{$$9, $$8, $$7, $$4, $$3, $$6, $$5});
            $$1 += $$5;
        }
        LOGGER.info("Total blocking time: {} ms", (Object)$$1);
        return $$0;
    }

    public static final class State
    extends Record {
        final String name;
        final AtomicLong preparationNanos;
        final AtomicLong preparationCount;
        final AtomicLong reloadNanos;
        final AtomicLong reloadCount;

        public State(String $$0, AtomicLong $$1, AtomicLong $$2, AtomicLong $$3, AtomicLong $$4) {
            this.name = $$0;
            this.preparationNanos = $$1;
            this.preparationCount = $$2;
            this.reloadNanos = $$3;
            this.reloadCount = $$4;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{State.class, "name;preparationNanos;preparationCount;reloadNanos;reloadCount", "name", "preparationNanos", "preparationCount", "reloadNanos", "reloadCount"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{State.class, "name;preparationNanos;preparationCount;reloadNanos;reloadCount", "name", "preparationNanos", "preparationCount", "reloadNanos", "reloadCount"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{State.class, "name;preparationNanos;preparationCount;reloadNanos;reloadCount", "name", "preparationNanos", "preparationCount", "reloadNanos", "reloadCount"}, this, $$0);
        }

        public String name() {
            return this.name;
        }

        public AtomicLong preparationNanos() {
            return this.preparationNanos;
        }

        public AtomicLong preparationCount() {
            return this.preparationCount;
        }

        public AtomicLong reloadNanos() {
            return this.reloadNanos;
        }

        public AtomicLong reloadCount() {
            return this.reloadCount;
        }
    }
}

