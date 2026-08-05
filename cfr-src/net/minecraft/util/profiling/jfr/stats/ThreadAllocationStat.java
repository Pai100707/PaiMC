/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 */
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
        String $$2 = $$1 == null ? UNKNOWN_THREAD : (String)MoreObjects.firstNonNull((Object)$$1.getJavaName(), (Object)UNKNOWN_THREAD);
        return new ThreadAllocationStat($$0.getStartTime(), $$2, $$0.getLong("allocated"));
    }

    public static Summary summary(List<ThreadAllocationStat> $$02) {
        TreeMap<String, Double> $$12 = new TreeMap<String, Double>();
        Map<String, List<ThreadAllocationStat>> $$22 = $$02.stream().collect(Collectors.groupingBy($$0 -> $$0.threadName));
        $$22.forEach(($$1, $$2) -> {
            if ($$2.size() < 2) {
                return;
            }
            ThreadAllocationStat $$3 = (ThreadAllocationStat)$$2.get(0);
            ThreadAllocationStat $$4 = (ThreadAllocationStat)$$2.get($$2.size() - 1);
            long $$5 = Duration.between($$3.timestamp, $$4.timestamp).getSeconds();
            long $$6 = $$4.totalBytes - $$3.totalBytes;
            $$12.put((String)$$1, (double)$$6 / (double)$$5);
        });
        return new Summary($$12);
    }

    public record Summary(Map<String, Double> allocationsPerSecondByThread) {
    }
}

