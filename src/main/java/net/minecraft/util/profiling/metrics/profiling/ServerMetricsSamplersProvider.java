package net.minecraft.util.profiling.metrics.profiling;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
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
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class ServerMetricsSamplersProvider implements MetricsSamplerProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Set<MetricSampler> samplers = new ObjectOpenHashSet();
   private final ProfilerSamplerAdapter samplerFactory = new ProfilerSamplerAdapter();

   public ServerMetricsSamplersProvider(LongSupplier $$0, boolean $$1) {
      this.samplers.add(tickTimeSampler($$0));
      if ($$1) {
         this.samplers.addAll(runtimeIndependentSamplers());
      }
   }

   public static Set<MetricSampler> runtimeIndependentSamplers() {
      Builder<MetricSampler> $$0 = ImmutableSet.builder();

      try {
         ServerMetricsSamplersProvider.CpuStats $$1 = new ServerMetricsSamplersProvider.CpuStats();
         IntStream.range(0, $$1.nrOfCpus)
            .mapToObj($$1x -> MetricSampler.create("cpu#" + $$1x, MetricCategory.CPU, () -> $$1.loadForCpu($$1)))
            .forEach($$0::add);
      } catch (Throwable var2) {
         LOGGER.warn("Failed to query cpu, no cpu stats will be recorded", var2);
      }

      $$0.add(
         MetricSampler.create(
            "heap MiB", MetricCategory.JVM, () -> SystemReport.sizeInMiB(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
         )
      );
      $$0.addAll(MetricsRegistry.INSTANCE.getRegisteredSamplers());
      return $$0.build();
   }

   @Override
   public Set<MetricSampler> samplers(Supplier<ProfileCollector> $$0) {
      this.samplers.addAll(this.samplerFactory.newSamplersFoundInProfiler($$0));
      return this.samplers;
   }

   public static MetricSampler tickTimeSampler(final LongSupplier $$0) {
      Stopwatch $$1 = Stopwatch.createUnstarted(new Ticker() {
         public long read() {
            return $$0.getAsLong();
         }
      });
      ToDoubleFunction<Stopwatch> $$2 = $$0x -> {
         if ($$0x.isRunning()) {
            $$0x.stop();
         }

         long $$1x = $$0x.elapsed(TimeUnit.NANOSECONDS);
         $$0x.reset();
         return $$1x;
      };
      MetricSampler.ValueIncreasedByPercentage $$3 = new MetricSampler.ValueIncreasedByPercentage(2.0F);
      return MetricSampler.builder("ticktime", MetricCategory.TICK_LOOP, $$2, $$1).withBeforeTick(Stopwatch::start).withThresholdAlert($$3).build();
   }

   static class CpuStats {
      private final SystemInfo systemInfo = new SystemInfo();
      private final CentralProcessor processor = this.systemInfo.getHardware().getProcessor();
      public final int nrOfCpus = this.processor.getLogicalProcessorCount();
      private long[][] previousCpuLoadTick = this.processor.getProcessorCpuLoadTicks();
      private double[] currentLoad = this.processor.getProcessorCpuLoadBetweenTicks(this.previousCpuLoadTick);
      private long lastPollMs;

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
