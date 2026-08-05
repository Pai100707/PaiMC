/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public record GcHeapStat(Instant timestamp, long heapUsed, Timing timing) {
    public static GcHeapStat from(RecordedEvent $$0) {
        return new GcHeapStat($$0.getStartTime(), $$0.getLong("heapUsed"), $$0.getString("when").equalsIgnoreCase("before gc") ? Timing.BEFORE_GC : Timing.AFTER_GC);
    }

    public static Summary summary(Duration $$0, List<GcHeapStat> $$1, Duration $$2, int $$3) {
        return new Summary($$0, $$2, $$3, GcHeapStat.calculateAllocationRatePerSecond($$1));
    }

    private static double calculateAllocationRatePerSecond(List<GcHeapStat> $$02) {
        long $$1 = 0L;
        Map<Timing, List<GcHeapStat>> $$2 = $$02.stream().collect(Collectors.groupingBy($$0 -> $$0.timing));
        List<GcHeapStat> $$3 = $$2.get((Object)Timing.BEFORE_GC);
        List<GcHeapStat> $$4 = $$2.get((Object)Timing.AFTER_GC);
        for (int $$5 = 1; $$5 < $$3.size(); ++$$5) {
            GcHeapStat $$6 = $$3.get($$5);
            GcHeapStat $$7 = $$4.get($$5 - 1);
            $$1 += $$6.heapUsed - $$7.heapUsed;
        }
        Duration $$8 = Duration.between($$02.get((int)1).timestamp, $$02.get((int)($$02.size() - 1)).timestamp);
        return (double)$$1 / (double)$$8.getSeconds();
    }

    static enum Timing {
        BEFORE_GC,
        AFTER_GC;

    }

    public record Summary(Duration duration, Duration gcTotalDuration, int totalGCs, double allocationRateBytesPerSecond) {
        public float gcOverHead() {
            return (float)this.gcTotalDuration.toMillis() / (float)this.duration.toMillis();
        }
    }
}

