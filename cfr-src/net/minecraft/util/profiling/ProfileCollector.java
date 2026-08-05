/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiling;

import java.util.Set;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

public interface ProfileCollector
extends ProfilerFiller {
    public ProfileResults getResults();

    public @Nullable ActiveProfiler.PathEntry getEntry(String var1);

    public Set<Pair<String, MetricCategory>> getChartedPaths();
}

