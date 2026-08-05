/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiling.metrics;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.ProfilerMeasured;
import org.jspecify.annotations.Nullable;

public class MetricsRegistry {
    public static final MetricsRegistry INSTANCE = new MetricsRegistry();
    private final WeakHashMap<ProfilerMeasured, Void> measuredInstances = new WeakHashMap();

    private MetricsRegistry() {
    }

    public void add(ProfilerMeasured $$0) {
        this.measuredInstances.put($$0, null);
    }

    public List<MetricSampler> getRegisteredSamplers() {
        Map<String, List<MetricSampler>> $$02 = this.measuredInstances.keySet().stream().flatMap($$0 -> $$0.profiledMetrics().stream()).collect(Collectors.groupingBy(MetricSampler::getName));
        return MetricsRegistry.aggregateDuplicates($$02);
    }

    private static List<MetricSampler> aggregateDuplicates(Map<String, List<MetricSampler>> $$02) {
        return $$02.entrySet().stream().map($$0 -> {
            String $$1 = (String)$$0.getKey();
            List $$2 = (List)$$0.getValue();
            return $$2.size() > 1 ? new AggregatedMetricSampler($$1, $$2) : (MetricSampler)$$2.get(0);
        }).collect(Collectors.toList());
    }

    static class AggregatedMetricSampler
    extends MetricSampler {
        private final List<MetricSampler> delegates;

        AggregatedMetricSampler(String $$0, List<MetricSampler> $$1) {
            super($$0, $$1.get(0).getCategory(), () -> AggregatedMetricSampler.averageValueFromDelegates($$1), () -> AggregatedMetricSampler.beforeTick($$1), AggregatedMetricSampler.thresholdTest($$1));
            this.delegates = $$1;
        }

        private static MetricSampler.ThresholdTest thresholdTest(List<MetricSampler> $$0) {
            return $$12 -> $$0.stream().anyMatch($$1 -> {
                if ($$1.thresholdTest != null) {
                    return $$1.thresholdTest.test($$12);
                }
                return false;
            });
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
            return $$1 / (double)$$0.size();
        }

        @Override
        public boolean equals(@Nullable Object $$0) {
            if (this == $$0) {
                return true;
            }
            if ($$0 == null || this.getClass() != $$0.getClass()) {
                return false;
            }
            if (!super.equals($$0)) {
                return false;
            }
            AggregatedMetricSampler $$1 = (AggregatedMetricSampler)$$0;
            return this.delegates.equals($$1.delegates);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.delegates);
        }
    }
}

