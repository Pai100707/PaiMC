package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

public record FileIOStat(Duration duration, @Nullable String path, long bytes) {
   public static FileIOStat.Summary summary(Duration $$0, List<FileIOStat> $$1) {
      long $$2 = $$1.stream().mapToLong($$0x -> $$0x.bytes).sum();
      return new FileIOStat.Summary(
         $$2,
         (double)$$2 / $$0.getSeconds(),
         $$1.size(),
         (double)$$1.size() / $$0.getSeconds(),
         $$1.stream().map(FileIOStat::duration).reduce(Duration.ZERO, Duration::plus),
         $$1.stream()
            .filter($$0x -> $$0x.path != null)
            .collect(Collectors.groupingBy($$0x -> $$0x.path, Collectors.summingLong($$0x -> $$0x.bytes)))
            .entrySet()
            .stream()
            .sorted(Entry.<String, Long>comparingByValue().reversed())
            .map($$0x -> Pair.of((String)$$0x.getKey(), (Long)$$0x.getValue()))
            .limit(10L)
            .toList()
      );
   }

   public record Summary(
      long totalBytes,
      double bytesPerSecond,
      long counts,
      double countsPerSecond,
      Duration timeSpentInIO,
      List<Pair<String, Long>> topTenContributorsByTotalBytes
   ) {
   }
}
