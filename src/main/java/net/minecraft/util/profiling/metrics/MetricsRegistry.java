package net.minecraft.util.profiling.metrics;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

public class MetricsRegistry {
   public static final MetricsRegistry INSTANCE = new MetricsRegistry();
   private final WeakHashMap<ProfilerMeasured, Void> measuredInstances = new WeakHashMap<>();

   private MetricsRegistry() {
   }

   public void add(ProfilerMeasured $$0) {
      this.measuredInstances.put($$0, null);
   }

   public List<MetricSampler> getRegisteredSamplers() {
      Map<String, List<MetricSampler>> $$0 = this.measuredInstances
         .keySet()
         .stream()
         .flatMap($$0x -> $$0x.profiledMetrics().stream())
         .collect(Collectors.groupingBy(MetricSampler::getName));
      return aggregateDuplicates($$0);
   }

   private static List<MetricSampler> aggregateDuplicates(Map<String, List<MetricSampler>> $$0) {
      return $$0.entrySet().stream().map($$0x -> {
         String $$1 = (String)$$0x.getKey();
         List<MetricSampler> $$2 = (List<MetricSampler>)$$0x.getValue();
         return (MetricSampler)($$2.size() > 1 ? new MetricsRegistry.AggregatedMetricSampler($$1, $$2) : $$2.get(0));
      }).collect(Collectors.toList());
   }

   static class AggregatedMetricSampler extends MetricSampler {
      private final List<MetricSampler> delegates;

      AggregatedMetricSampler(String $$0, List<MetricSampler> $$1) {
         super($$0, $$1.get(0).getCategory(), () -> averageValueFromDelegates($$1), () -> beforeTick($$1), thresholdTest($$1));
         this.delegates = $$1;
      }

      private static MetricSampler.ThresholdTest thresholdTest(List<MetricSampler> $$0) {
         return $$1 -> $$0.stream().anyMatch($$1x -> $$1x.thresholdTest != null ? $$1x.thresholdTest.test($$1) : false);
      }

      private static void beforeTick(List<MetricSampler> $$0) {
         for (MetricSampler $$1 : $$0) {
            $$1.onStartTick();
         }
      }

      private static double averageValueFromDelegates(List<MetricSampler> $$0) {
         double $$1 = 0.0;

         for (MetricSampler $$2 : $$0) {
            $$1 += $$2.getSampler().getAsDouble();
         }

         return $$1 / $$0.size();
      }

      @Override
      public boolean equals(@Nullable Object $$0) {
         if (this == $$0) {
            return true;
         } else if ($$0 == null || this.getClass() != $$0.getClass()) {
            return false;
         } else if (!super.equals($$0)) {
            return false;
         } else {
            MetricsRegistry.AggregatedMetricSampler $$1 = (MetricsRegistry.AggregatedMetricSampler)$$0;
            return this.delegates.equals($$1.delegates);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(super.hashCode(), this.delegates);
      }
   }
}
