package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public record GcHeapStat(Instant timestamp, long heapUsed, GcHeapStat.Timing timing) {
   public static GcHeapStat from(RecordedEvent $$0) {
      return new GcHeapStat(
         $$0.getStartTime(),
         $$0.getLong("heapUsed"),
         $$0.getString("when").equalsIgnoreCase("before gc") ? GcHeapStat.Timing.BEFORE_GC : GcHeapStat.Timing.AFTER_GC
      );
   }

   public static GcHeapStat.Summary summary(Duration $$0, List<GcHeapStat> $$1, Duration $$2, int $$3) {
      return new GcHeapStat.Summary($$0, $$2, $$3, calculateAllocationRatePerSecond($$1));
   }

   private static double calculateAllocationRatePerSecond(List<GcHeapStat> $$0) {
      long $$1 = 0L;
      Map<GcHeapStat.Timing, List<GcHeapStat>> $$2 = $$0.stream().collect(Collectors.groupingBy($$0x -> $$0x.timing));
      List<GcHeapStat> $$3 = $$2.get(GcHeapStat.Timing.BEFORE_GC);
      List<GcHeapStat> $$4 = $$2.get(GcHeapStat.Timing.AFTER_GC);

      for (int $$5 = 1; $$5 < $$3.size(); $$5++) {
         GcHeapStat $$6 = $$3.get($$5);
         GcHeapStat $$7 = $$4.get($$5 - 1);
         $$1 += $$6.heapUsed - $$7.heapUsed;
      }

      Duration $$8 = Duration.between($$0.get(1).timestamp, $$0.get($$0.size() - 1).timestamp);
      return (double)$$1 / $$8.getSeconds();
   }

   public record Summary(Duration duration, Duration gcTotalDuration, int totalGCs, double allocationRateBytesPerSecond) {
      public float gcOverHead() {
         return (float)this.gcTotalDuration.toMillis() / (float)this.duration.toMillis();
      }
   }

   static enum Timing {
      BEFORE_GC,
      AFTER_GC;
   }
}
