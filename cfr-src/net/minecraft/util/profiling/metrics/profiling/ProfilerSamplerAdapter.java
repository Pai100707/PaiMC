/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 */
package net.minecraft.util.profiling.metrics.profiling;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;

public class ProfilerSamplerAdapter {
    private final Set<String> previouslyFoundSamplerNames = new ObjectOpenHashSet();

    public Set<MetricSampler> newSamplersFoundInProfiler(Supplier<ProfileCollector> $$02) {
        Set<MetricSampler> $$12 = $$02.get().getChartedPaths().stream().filter($$0 -> !this.previouslyFoundSamplerNames.contains($$0.getLeft())).map($$1 -> ProfilerSamplerAdapter.samplerForProfilingPath($$02, (String)$$1.getLeft(), (MetricCategory)((Object)((Object)$$1.getRight())))).collect(Collectors.toSet());
        for (MetricSampler $$2 : $$12) {
            this.previouslyFoundSamplerNames.add($$2.getName());
        }
        return $$12;
    }

    private static MetricSampler samplerForProfilingPath(Supplier<ProfileCollector> $$0, String $$1, MetricCategory $$2) {
        return MetricSampler.create($$1, $$2, () -> {
            ActiveProfiler.PathEntry $$2 = ((ProfileCollector)$$0.get()).getEntry($$1);
            return $$2 == null ? 0.0 : (double)$$2.getMaxDuration() / (double)TimeUtil.NANOSECONDS_PER_MILLISECOND;
        });
    }
}

