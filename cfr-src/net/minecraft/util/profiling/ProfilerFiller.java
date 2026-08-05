/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.util.profiling.metrics.MetricCategory;

public interface ProfilerFiller {
    public static final String ROOT = "root";

    public void startTick();

    public void endTick();

    public void push(String var1);

    public void push(Supplier<String> var1);

    public void pop();

    public void popPush(String var1);

    public void popPush(Supplier<String> var1);

    default public void addZoneText(String $$0) {
    }

    default public void addZoneValue(long $$0) {
    }

    default public void setZoneColor(int $$0) {
    }

    default public Zone zone(String $$0) {
        this.push($$0);
        return new Zone(this);
    }

    default public Zone zone(Supplier<String> $$0) {
        this.push($$0);
        return new Zone(this);
    }

    public void markForCharting(MetricCategory var1);

    default public void incrementCounter(String $$0) {
        this.incrementCounter($$0, 1);
    }

    public void incrementCounter(String var1, int var2);

    default public void incrementCounter(Supplier<String> $$0) {
        this.incrementCounter($$0, 1);
    }

    public void incrementCounter(Supplier<String> var1, int var2);

    public static ProfilerFiller combine(ProfilerFiller $$0, ProfilerFiller $$1) {
        if ($$0 == InactiveProfiler.INSTANCE) {
            return $$1;
        }
        if ($$1 == InactiveProfiler.INSTANCE) {
            return $$0;
        }
        return new CombinedProfileFiller($$0, $$1);
    }

    public static class CombinedProfileFiller
    implements ProfilerFiller {
        private final ProfilerFiller first;
        private final ProfilerFiller second;

        public CombinedProfileFiller(ProfilerFiller $$0, ProfilerFiller $$1) {
            this.first = $$0;
            this.second = $$1;
        }

        @Override
        public void startTick() {
            this.first.startTick();
            this.second.startTick();
        }

        @Override
        public void endTick() {
            this.first.endTick();
            this.second.endTick();
        }

        @Override
        public void push(String $$0) {
            this.first.push($$0);
            this.second.push($$0);
        }

        @Override
        public void push(Supplier<String> $$0) {
            this.first.push($$0);
            this.second.push($$0);
        }

        @Override
        public void markForCharting(MetricCategory $$0) {
            this.first.markForCharting($$0);
            this.second.markForCharting($$0);
        }

        @Override
        public void pop() {
            this.first.pop();
            this.second.pop();
        }

        @Override
        public void popPush(String $$0) {
            this.first.popPush($$0);
            this.second.popPush($$0);
        }

        @Override
        public void popPush(Supplier<String> $$0) {
            this.first.popPush($$0);
            this.second.popPush($$0);
        }

        @Override
        public void incrementCounter(String $$0, int $$1) {
            this.first.incrementCounter($$0, $$1);
            this.second.incrementCounter($$0, $$1);
        }

        @Override
        public void incrementCounter(Supplier<String> $$0, int $$1) {
            this.first.incrementCounter($$0, $$1);
            this.second.incrementCounter($$0, $$1);
        }

        @Override
        public void addZoneText(String $$0) {
            this.first.addZoneText($$0);
            this.second.addZoneText($$0);
        }

        @Override
        public void addZoneValue(long $$0) {
            this.first.addZoneValue($$0);
            this.second.addZoneValue($$0);
        }

        @Override
        public void setZoneColor(int $$0) {
            this.first.setZoneColor($$0);
            this.second.setZoneColor($$0);
        }
    }
}

