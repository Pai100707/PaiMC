package net.minecraft.util.profiling.metrics.profiling;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;

public class ProfilerSamplerAdapter {
   private final Set<String> previouslyFoundSamplerNames = new ObjectOpenHashSet();

   public Set<MetricSampler> newSamplersFoundInProfiler(Supplier<ProfileCollector> $$0) {
      Set<MetricSampler> $$1 = $$0.get()
         .getChartedPaths()
         .stream()
         .filter($$0x -> !this.previouslyFoundSamplerNames.contains($$0x.getLeft()))
         .map($$1x -> samplerForProfilingPath($$0, (String)$$1x.getLeft(), (MetricCategory)$$1x.getRight()))
         .collect(Collectors.toSet());

      for (MetricSampler $$2 : $$1) {
         this.previouslyFoundSamplerNames.add($$2.getName());
      }

      return $$1;
   }

   private static MetricSampler samplerForProfilingPath(Supplier<ProfileCollector> $$0, String $$1, MetricCategory $$2) {
      return MetricSampler.create($$1, $$2, () -> {
         ActiveProfiler.PathEntry $$2x = $$0.get().getEntry($$1);
         return $$2x == null ? 0.0 : (double)$$2x.getMaxDuration() / net.minecraft.util.TimeUtil.NANOSECONDS_PER_MILLISECOND;
      });
   }
}
