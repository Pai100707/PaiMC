/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.google.common.base.Ticker
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  org.slf4j.Logger
 *  oshi.SystemInfo
 *  oshi.hardware.CentralProcessor
 */
package net.minecraft.util.profiling.metrics.profiling;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import net.minecraft.SystemReport;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import net.minecraft.util.profiling.metrics.profiling.ProfilerSamplerAdapter;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class ServerMetricsSamplersProvider
implements MetricsSamplerProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Set<MetricSampler> samplers = new ObjectOpenHashSet();
    private final ProfilerSamplerAdapter samplerFactory = new ProfilerSamplerAdapter();

    public ServerMetricsSamplersProvider(LongSupplier $$0, boolean $$1) {
        this.samplers.add(ServerMetricsSamplersProvider.tickTimeSampler($$0));
        if ($$1) {
            this.samplers.addAll(ServerMetricsSamplersProvider.runtimeIndependentSamplers());
        }
    }

    public static Set<MetricSampler> runtimeIndependentSamplers() {
        ImmutableSet.Builder $$0 = ImmutableSet.builder();
        try {
            CpuStats $$12 = new CpuStats();
            IntStream.range(0, $$12.nrOfCpus).mapToObj($$1 -> MetricSampler.create("cpu#" + $$1, MetricCategory.CPU, () -> $$12.loadForCpu($$1))).forEach(arg_0 -> ((ImmutableSet.Builder)$$0).add(arg_0));
        }
        catch (Throwable $$2) {
            LOGGER.warn("Failed to query cpu, no cpu stats will be recorded", $$2);
        }
        $$0.add((Object)MetricSampler.create("heap MiB", MetricCategory.JVM, () -> SystemReport.sizeInMiB(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())));
        $$0.addAll(MetricsRegistry.INSTANCE.getRegisteredSamplers());
        return $$0.build();
    }

    @Override
    public Set<MetricSampler> samplers(Supplier<ProfileCollector> $$0) {
        this.samplers.addAll(this.samplerFactory.newSamplersFoundInProfiler($$0));
        return this.samplers;
    }

    public static MetricSampler tickTimeSampler(final LongSupplier $$02) {
        Stopwatch $$1 = Stopwatch.createUnstarted((Ticker)new Ticker(){

            public long read() {
                return $$02.getAsLong();
            }
        });
        ToDoubleFunction<Stopwatch> $$2 = $$0 -> {
            if ($$0.isRunning()) {
                $$0.stop();
            }
            long $$1 = $$0.elapsed(TimeUnit.NANOSECONDS);
            $$0.reset();
            return $$1;
        };
        MetricSampler.ValueIncreasedByPercentage $$3 = new MetricSampler.ValueIncreasedByPercentage(2.0f);
        return MetricSampler.builder("ticktime", MetricCategory.TICK_LOOP, $$2, $$1).withBeforeTick(Stopwatch::start).withThresholdAlert($$3).build();
    }

    static class CpuStats {
        private final SystemInfo systemInfo = new SystemInfo();
        private final CentralProcessor processor = this.systemInfo.getHardware().getProcessor();
        public final int nrOfCpus = this.processor.getLogicalProcessorCount();
        private long[][] previousCpuLoadTick = this.processor.getProcessorCpuLoadTicks();
        private double[] currentLoad = this.processor.getProcessorCpuLoadBetweenTicks(this.previousCpuLoadTick);
        private long lastPollMs;

        CpuStats() {
        }

        public double loadForCpu(int $$0) {
            long $$1 = System.currentTimeMillis();
            if (this.lastPollMs == 0L || this.lastPollMs + 501L < $$1) {
                this.currentLoad = this.processor.getProcessorCpuLoadBetweenTicks(this.previousCpuLoadTick);
                this.previousCpuLoadTick = this.processor.getProcessorCpuLoadTicks();
                this.lastPollMs = $$1;
            }
            return this.currentLoad[$$0] * 100.0;
        }
    }
}

