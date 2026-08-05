/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.EasingType;
import net.minecraft.util.Keyframe;
import net.minecraft.util.KeyframeTrack;
import net.minecraft.world.attribute.LerpFunction;

public class KeyframeTrackSampler<T> {
    private final Optional<Integer> periodTicks;
    private final LerpFunction<T> lerp;
    private final List<Segment<T>> segments;

    KeyframeTrackSampler(KeyframeTrack<T> $$0, Optional<Integer> $$1, LerpFunction<T> $$2) {
        this.periodTicks = $$1;
        this.lerp = $$2;
        this.segments = KeyframeTrackSampler.bakeSegments($$0, $$1);
    }

    private static <T> List<Segment<T>> bakeSegments(KeyframeTrack<T> $$0, Optional<Integer> $$1) {
        List<Keyframe<T>> $$2 = $$0.keyframes();
        if ($$2.size() == 1) {
            T $$3 = $$2.getFirst().value();
            return List.of(new Segment<T>(EasingType.CONSTANT, $$3, 0, $$3, 0));
        }
        ArrayList<Segment<T>> $$4 = new ArrayList<Segment<T>>();
        if ($$1.isPresent()) {
            Keyframe<T> $$5 = $$2.getFirst();
            Keyframe<T> $$6 = $$2.getLast();
            $$4.add(new Segment<T>($$0, $$6, $$6.ticks() - $$1.get(), $$5, $$5.ticks()));
            KeyframeTrackSampler.addSegmentsFromKeyframes($$0, $$2, $$4);
            $$4.add(new Segment<T>($$0, $$6, $$6.ticks(), $$5, $$5.ticks() + $$1.get()));
        } else {
            KeyframeTrackSampler.addSegmentsFromKeyframes($$0, $$2, $$4);
        }
        return List.copyOf($$4);
    }

    private static <T> void addSegmentsFromKeyframes(KeyframeTrack<T> $$0, List<Keyframe<T>> $$1, List<Segment<T>> $$2) {
        for (int $$3 = 0; $$3 < $$1.size() - 1; ++$$3) {
            Keyframe<T> $$4 = $$1.get($$3);
            Keyframe<T> $$5 = $$1.get($$3 + 1);
            $$2.add(new Segment<T>($$0, $$4, $$4.ticks(), $$5, $$5.ticks()));
        }
    }

    public T sample(long $$0) {
        long $$1 = this.loopTicks($$0);
        Segment<T> $$2 = this.getSegmentAt($$1);
        if ($$1 <= (long)$$2.fromTicks) {
            return $$2.fromValue;
        }
        if ($$1 >= (long)$$2.toTicks) {
            return $$2.toValue;
        }
        float $$3 = (float)($$1 - (long)$$2.fromTicks) / (float)($$2.toTicks - $$2.fromTicks);
        float $$4 = $$2.easing.apply($$3);
        return this.lerp.apply($$4, $$2.fromValue, $$2.toValue);
    }

    private Segment<T> getSegmentAt(long $$0) {
        for (Segment<T> $$1 : this.segments) {
            if ($$0 >= (long)$$1.toTicks) continue;
            return $$1;
        }
        return this.segments.getLast();
    }

    private long loopTicks(long $$0) {
        if (this.periodTicks.isPresent()) {
            return Math.floorMod($$0, (int)this.periodTicks.get());
        }
        return $$0;
    }

    static final class Segment<T>
    extends Record {
        final EasingType easing;
        final T fromValue;
        final int fromTicks;
        final T toValue;
        final int toTicks;

        public Segment(KeyframeTrack<T> $$0, Keyframe<T> $$1, int $$2, Keyframe<T> $$3, int $$4) {
            this($$0.easingType(), $$1.value(), $$2, $$3.value(), $$4);
        }

        Segment(EasingType $$0, T $$1, int $$2, T $$3, int $$4) {
            this.easing = $$0;
            this.fromValue = $$1;
            this.fromTicks = $$2;
            this.toValue = $$3;
            this.toTicks = $$4;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Segment.class, "easing;fromValue;fromTicks;toValue;toTicks", "easing", "fromValue", "fromTicks", "toValue", "toTicks"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Segment.class, "easing;fromValue;fromTicks;toValue;toTicks", "easing", "fromValue", "fromTicks", "toValue", "toTicks"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Segment.class, "easing;fromValue;fromTicks;toValue;toTicks", "easing", "fromValue", "fromTicks", "toValue", "toTicks"}, this, $$0);
        }

        public EasingType easing() {
            return this.easing;
        }

        public T fromValue() {
            return this.fromValue;
        }

        public int fromTicks() {
            return this.fromTicks;
        }

        public T toValue() {
            return this.toValue;
        }

        public int toTicks() {
            return this.toTicks;
        }
    }
}

