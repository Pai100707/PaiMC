package net.minecraft.util.profiling;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.function.LongSupplier;
import net.minecraft.SharedConstants;
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
      this.tick++;
      return this.profiler;
   }

   public void endTick() {
      if (this.profiler != InactiveProfiler.INSTANCE) {
         ProfileResults $$0 = this.profiler.getResults();
         this.profiler = InactiveProfiler.INSTANCE;
         if ($$0.getNanoDuration() >= this.saveThreshold) {
            File $$1 = new File(this.location, "tick-results-" + net.minecraft.util.Util.getFilenameFormattedDateTime() + ".txt");
            $$0.saveResults($$1.toPath());
            LOGGER.info("Recorded long tick -- wrote info to: {}", $$1.getAbsolutePath());
         }
      }
   }

   @Nullable
   public static SingleTickProfiler createTickProfiler(String $$0) {
      return SharedConstants.DEBUG_MONITOR_TICK_TIMES
         ? new SingleTickProfiler(net.minecraft.util.Util.timeSource, $$0, SharedConstants.MAXIMUM_TICK_TIME_NANOS)
         : null;
   }

   public static ProfilerFiller decorateFiller(ProfilerFiller $$0, @Nullable SingleTickProfiler $$1) {
      return $$1 != null ? ProfilerFiller.combine($$1.startTick(), $$0) : $$0;
   }
}
