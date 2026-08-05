/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.Plot
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util.profiling;

import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.slf4j.Logger;

public class TracyZoneFiller
implements ProfilerFiller {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 5);
    private final List<Zone> activeZones = new ArrayList<Zone>();
    private final Map<String, PlotAndValue> plots = new HashMap<String, PlotAndValue>();
    private final String name = Thread.currentThread().getName();

    @Override
    public void startTick() {
    }

    @Override
    public void endTick() {
        for (PlotAndValue $$0 : this.plots.values()) {
            $$0.set(0);
        }
    }

    @Override
    public void push(String $$0) {
        Optional $$4;
        String $$1 = "";
        String $$2 = "";
        int $$3 = 0;
        if (SharedConstants.IS_RUNNING_IN_IDE && ($$4 = STACK_WALKER.walk($$02 -> $$02.filter($$0 -> $$0.getDeclaringClass() != TracyZoneFiller.class && $$0.getDeclaringClass() != ProfilerFiller.CombinedProfileFiller.class).findFirst())).isPresent()) {
            StackWalker.StackFrame $$5 = (StackWalker.StackFrame)$$4.get();
            $$1 = $$5.getMethodName();
            $$2 = $$5.getFileName();
            $$3 = $$5.getLineNumber();
        }
        Zone $$6 = TracyClient.beginZone((String)$$0, (String)$$1, (String)$$2, (int)$$3);
        this.activeZones.add($$6);
    }

    @Override
    public void push(Supplier<String> $$0) {
        this.push($$0.get());
    }

    @Override
    public void pop() {
        if (this.activeZones.isEmpty()) {
            LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
            return;
        }
        Zone $$0 = this.activeZones.removeLast();
        $$0.close();
    }

    @Override
    public void popPush(String $$0) {
        this.pop();
        this.push($$0);
    }

    @Override
    public void popPush(Supplier<String> $$0) {
        this.pop();
        this.push($$0.get());
    }

    @Override
    public void markForCharting(MetricCategory $$0) {
    }

    @Override
    public void incrementCounter(String $$0, int $$12) {
        this.plots.computeIfAbsent($$0, $$1 -> new PlotAndValue(this.name + " " + $$0)).add($$12);
    }

    @Override
    public void incrementCounter(Supplier<String> $$0, int $$1) {
        this.incrementCounter($$0.get(), $$1);
    }

    private Zone activeZone() {
        return this.activeZones.getLast();
    }

    @Override
    public void addZoneText(String $$0) {
        this.activeZone().addText($$0);
    }

    @Override
    public void addZoneValue(long $$0) {
        this.activeZone().addValue($$0);
    }

    @Override
    public void setZoneColor(int $$0) {
        this.activeZone().setColor($$0);
    }

    static final class PlotAndValue {
        private final Plot plot;
        private int value;

        PlotAndValue(String $$0) {
            this.plot = TracyClient.createPlot((String)$$0);
            this.value = 0;
        }

        void set(int $$0) {
            this.value = $$0;
            this.plot.setValue((double)$$0);
        }

        void add(int $$0) {
            this.set(this.value + $$0);
        }
    }
}

