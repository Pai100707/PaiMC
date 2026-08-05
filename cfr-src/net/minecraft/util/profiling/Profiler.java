/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiling;

import com.mojang.jtracy.TracyClient;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.TracyZoneFiller;
import org.jspecify.annotations.Nullable;

public final class Profiler {
    private static final ThreadLocal<TracyZoneFiller> TRACY_FILLER = ThreadLocal.withInitial(TracyZoneFiller::new);
    private static final ThreadLocal<@Nullable ProfilerFiller> ACTIVE = new ThreadLocal();
    private static final AtomicInteger ACTIVE_COUNT = new AtomicInteger();

    private Profiler() {
    }

    public static Scope use(ProfilerFiller $$0) {
        Profiler.startUsing($$0);
        return Profiler::stopUsing;
    }

    private static void startUsing(ProfilerFiller $$0) {
        if (ACTIVE.get() != null) {
            throw new IllegalStateException("Profiler is already active");
        }
        ProfilerFiller $$1 = Profiler.decorateFiller($$0);
        ACTIVE.set($$1);
        ACTIVE_COUNT.incrementAndGet();
        $$1.startTick();
    }

    private static void stopUsing() {
        ProfilerFiller $$0 = ACTIVE.get();
        if ($$0 == null) {
            throw new IllegalStateException("Profiler was not active");
        }
        ACTIVE.remove();
        ACTIVE_COUNT.decrementAndGet();
        $$0.endTick();
    }

    private static ProfilerFiller decorateFiller(ProfilerFiller $$0) {
        return ProfilerFiller.combine(Profiler.getDefaultFiller(), $$0);
    }

    public static ProfilerFiller get() {
        if (ACTIVE_COUNT.get() == 0) {
            return Profiler.getDefaultFiller();
        }
        return Objects.requireNonNullElseGet(ACTIVE.get(), Profiler::getDefaultFiller);
    }

    private static ProfilerFiller getDefaultFiller() {
        if (TracyClient.isAvailable()) {
            return TRACY_FILLER.get();
        }
        return InactiveProfiler.INSTANCE;
    }

    public static interface Scope
    extends AutoCloseable {
        @Override
        public void close();
    }
}

