/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

public record FileIOStat(Duration duration, @Nullable String path, long bytes) {
    public static Summary summary(Duration $$02, List<FileIOStat> $$1) {
        long $$2 = $$1.stream().mapToLong($$0 -> $$0.bytes).sum();
        return new Summary($$2, (double)$$2 / (double)$$02.getSeconds(), $$1.size(), (double)$$1.size() / (double)$$02.getSeconds(), $$1.stream().map(FileIOStat::duration).reduce(Duration.ZERO, Duration::plus), $$1.stream().filter($$0 -> $$0.path != null).collect(Collectors.groupingBy($$0 -> $$0.path, Collectors.summingLong($$0 -> $$0.bytes))).entrySet().stream().sorted(Map.Entry.comparingByValue().reversed()).map($$0 -> Pair.of((Object)((String)$$0.getKey()), (Object)((Long)$$0.getValue()))).limit(10L).toList());
    }

    public record Summary(long totalBytes, double bytesPerSecond, long counts, double countsPerSecond, Duration timeSpentInIO, List<Pair<String, Long>> topTenContributorsByTotalBytes) {
    }
}

