package net.minecraft.util.profiling.jfr.stats;

import com.google.common.base.MoreObjects;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;

public record ThreadAllocationStat(Instant timestamp, String threadName, long totalBytes) {
   private static final String UNKNOWN_THREAD = "unknown";

   public static ThreadAllocationStat from(RecordedEvent $$0) {
      RecordedThread $$1 = $$0.getThread("thread");
      String $$2 = $$1 == null ? "unknown" : (String)MoreObjects.firstNonNull($$1.getJavaName(), "unknown");
      return new ThreadAllocationStat($$0.getStartTime(), $$2, $$0.getLong("allocated"));
   }

   public static ThreadAllocationStat.Summary summary(List<ThreadAllocationStat> $$0) {
      Map<String, Double> $$1 = new TreeMap<>();
      Map<String, List<ThreadAllocationStat>> $$2 = $$0.stream().collect(Collectors.groupingBy($$0x -> $$0x.threadName));
      $$2.forEach(($$1x, $$2x) -> {
         if ($$2x.size() >= 2) {
            ThreadAllocationStat $$3 = (ThreadAllocationStat)$$2x.get(0);
            ThreadAllocationStat $$4 = (ThreadAllocationStat)$$2x.get($$2x.size() - 1);
            long $$5 = Duration.between($$3.timestamp, $$4.timestamp).getSeconds();
            long $$6 = $$4.totalBytes - $$3.totalBytes;
            $$1.put($$1x, (double)$$6 / $$5);
         }
      });
      return new ThreadAllocationStat.Summary($$1);
   }

   public record Summary(Map<String, Double> allocationsPerSecondByThread) {
   }
}
