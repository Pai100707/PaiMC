package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.profiling.jfr.Percentiles;
import org.jspecify.annotations.Nullable;

public record TimedStatSummary<T extends TimedStat>(
   T fastest, T slowest, @Nullable T secondSlowest, int count, Map<Integer, Double> percentilesNanos, Duration totalDuration
) {
   public static <T extends TimedStat> Optional<TimedStatSummary<T>> summary(List<T> $$0) {
      if ($$0.isEmpty()) {
         return Optional.empty();
      } else {
         List<T> $$1 = $$0.stream().sorted(Comparator.comparing(TimedStat::duration)).toList();
         Duration $$2 = $$1.stream().map(TimedStat::duration).reduce(Duration::plus).orElse(Duration.ZERO);
         T $$3 = (T)$$1.getFirst();
         T $$4 = (T)$$1.getLast();
         T $$5 = $$1.size() > 1 ? $$1.get($$1.size() - 2) : null;
         int $$6 = $$1.size();
         Map<Integer, Double> $$7 = Percentiles.evaluate($$1.stream().mapToLong($$0x -> $$0x.duration().toNanos()).toArray());
         return Optional.of(new TimedStatSummary<>($$3, $$4, $$5, $$6, $$7, $$2));
      }
   }
}
