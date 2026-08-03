package net.minecraft.util.profiling;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ActiveProfiler implements ProfileCollector {
   private static final long WARNING_TIME_NANOS = Duration.ofMillis(100L).toNanos();
   private static final Logger LOGGER = LogUtils.getLogger();
   private final List<String> paths = Lists.newArrayList();
   private final LongList startTimes = new LongArrayList();
   private final Map<String, ActiveProfiler.PathEntry> entries = Maps.newHashMap();
   private final IntSupplier getTickTime;
   private final LongSupplier getRealTime;
   private final long startTimeNano;
   private final int startTimeTicks;
   private String path = "";
   private boolean started;
   @Nullable
   private ActiveProfiler.PathEntry currentEntry;
   private final BooleanSupplier suppressWarnings;
   private final Set<Pair<String, MetricCategory>> chartedPaths = new ObjectArraySet();

   public ActiveProfiler(LongSupplier $$0, IntSupplier $$1, BooleanSupplier $$2) {
      this.startTimeNano = $$0.getAsLong();
      this.getRealTime = $$0;
      this.startTimeTicks = $$1.getAsInt();
      this.getTickTime = $$1;
      this.suppressWarnings = $$2;
   }

   @Override
   public void startTick() {
      if (this.started) {
         LOGGER.error("Profiler tick already started - missing endTick()?");
      } else {
         this.started = true;
         this.path = "";
         this.paths.clear();
         this.push("root");
      }
   }

   @Override
   public void endTick() {
      if (!this.started) {
         LOGGER.error("Profiler tick already ended - missing startTick()?");
      } else {
         this.pop();
         this.started = false;
         if (!this.path.isEmpty()) {
            LOGGER.error(
               "Profiler tick ended before path was fully popped (remainder: '{}'). Mismatched push/pop?",
               LogUtils.defer(() -> ProfileResults.demanglePath(this.path))
            );
         }
      }
   }

   @Override
   public void push(String $$0) {
      if (!this.started) {
         LOGGER.error("Cannot push '{}' to profiler if profiler tick hasn't started - missing startTick()?", $$0);
      } else {
         if (!this.path.isEmpty()) {
            this.path = this.path + "\u001e";
         }

         this.path = this.path + $$0;
         this.paths.add(this.path);
         this.startTimes.add(net.minecraft.util.Util.getNanos());
         this.currentEntry = null;
      }
   }

   @Override
   public void push(Supplier<String> $$0) {
      this.push($$0.get());
   }

   @Override
   public void markForCharting(MetricCategory $$0) {
      this.chartedPaths.add(Pair.of(this.path, $$0));
   }

   @Override
   public void pop() {
      if (!this.started) {
         LOGGER.error("Cannot pop from profiler if profiler tick hasn't started - missing startTick()?");
      } else if (this.startTimes.isEmpty()) {
         LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
      } else {
         long $$0 = net.minecraft.util.Util.getNanos();
         long $$1 = this.startTimes.removeLong(this.startTimes.size() - 1);
         this.paths.removeLast();
         long $$2 = $$0 - $$1;
         ActiveProfiler.PathEntry $$3 = this.getCurrentEntry();
         $$3.accumulatedDuration += $$2;
         $$3.count++;
         $$3.maxDuration = Math.max($$3.maxDuration, $$2);
         $$3.minDuration = Math.min($$3.minDuration, $$2);
         if ($$2 > WARNING_TIME_NANOS && !this.suppressWarnings.getAsBoolean()) {
            LOGGER.warn(
               "Something's taking too long! '{}' took aprox {} ms",
               LogUtils.defer(() -> ProfileResults.demanglePath(this.path)),
               LogUtils.defer(() -> $$2 / 1000000.0)
            );
         }

         this.path = this.paths.isEmpty() ? "" : this.paths.getLast();
         this.currentEntry = null;
      }
   }

   @Override
   public void popPush(String $$0) {
      this.pop();
      this.push($$0);
   }

   @Override
   public void popPush(Supplier<String> $$0) {
      this.pop();
      this.push($$0);
   }

   private ActiveProfiler.PathEntry getCurrentEntry() {
      if (this.currentEntry == null) {
         this.currentEntry = this.entries.computeIfAbsent(this.path, $$0 -> new ActiveProfiler.PathEntry());
      }

      return this.currentEntry;
   }

   @Override
   public void incrementCounter(String $$0, int $$1) {
      this.getCurrentEntry().counters.addTo($$0, $$1);
   }

   @Override
   public void incrementCounter(Supplier<String> $$0, int $$1) {
      this.getCurrentEntry().counters.addTo($$0.get(), $$1);
   }

   @Override
   public ProfileResults getResults() {
      return new FilledProfileResults(this.entries, this.startTimeNano, this.startTimeTicks, this.getRealTime.getAsLong(), this.getTickTime.getAsInt());
   }

   @Nullable
   @Override
   public ActiveProfiler.PathEntry getEntry(String $$0) {
      return this.entries.get($$0);
   }

   @Override
   public Set<Pair<String, MetricCategory>> getChartedPaths() {
      return this.chartedPaths;
   }

   public static class PathEntry implements ProfilerPathEntry {
      long maxDuration = Long.MIN_VALUE;
      long minDuration = Long.MAX_VALUE;
      long accumulatedDuration;
      long count;
      final Object2LongOpenHashMap<String> counters = new Object2LongOpenHashMap();

      @Override
      public long getDuration() {
         return this.accumulatedDuration;
      }

      @Override
      public long getMaxDuration() {
         return this.maxDuration;
      }

      @Override
      public long getCount() {
         return this.count;
      }

      @Override
      public Object2LongMap<String> getCounters() {
         return Object2LongMaps.unmodifiable(this.counters);
      }
   }
}
