/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.profiling;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.function.LongSupplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SingleTickProfiler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final LongSupplier realTime;
    private final long saveThreshold;
    private int tick;
    private final File location;
    private ProfileCollector profiler = InactiveProfiler.INSTANCE;

    public SingleTickProfiler(LongSupplier $$0, String $$1, long $$2) {
        this.realTime = $$0;
        this.location = new File("debug", $$1);
        this.saveThreshold = $$2;
    }

    public ProfilerFiller startTick() {
        this.profiler = new ActiveProfiler(this.realTime, () -> this.tick, () -> true);
        ++this.tick;
        return this.profiler;
    }

    public void endTick() {
        if (this.profiler == InactiveProfiler.INSTANCE) {
            return;
        }
        ProfileResults $$0 = this.profiler.getResults();
        this.profiler = InactiveProfiler.INSTANCE;
        if ($$0.getNanoDuration() >= this.saveThreshold) {
            File $$1 = new File(this.location, "tick-results-" + Util.getFilenameFormattedDateTime() + ".txt");
            $$0.saveResults($$1.toPath());
            LOGGER.info("Recorded long tick -- wrote info to: {}", (Object)$$1.getAbsolutePath());
        }
    }

    public static @Nullable SingleTickProfiler createTickProfiler(String $$0) {
        if (SharedConstants.DEBUG_MONITOR_TICK_TIMES) {
            return new SingleTickProfiler(Util.timeSource, $$0, SharedConstants.MAXIMUM_TICK_TIME_NANOS);
        }
        return null;
    }

    public static ProfilerFiller decorateFiller(ProfilerFiller $$0, @Nullable SingleTickProfiler $$1) {
        if ($$1 != null) {
            return ProfilerFiller.combine($$1.startTick(), $$0);
        }
        return $$0;
    }
}

