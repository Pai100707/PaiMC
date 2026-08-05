package net.minecraft.util.profiling;

import java.util.Set;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;

public interface ProfileCollector extends ProfilerFiller {
   ProfileResults getResults();

   
   ActiveProfiler.PathEntry getEntry(String var1);

   Set<Pair<String, MetricCategory>> getChartedPaths();
}
