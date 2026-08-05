/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.floats.FloatArrayList
 *  it.unimi.dsi.fastutil.floats.FloatList
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.util.BoundedFloatFunction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import org.apache.commons.lang3.mutable.MutableObject;

public interface CubicSpline<C, I extends BoundedFloatFunction<C>>
extends BoundedFloatFunction<C> {
    @VisibleForDebug
    public String parityString();

    public CubicSpline<C, I> mapAll(CoordinateVisitor<I> var1);

    public static <C, I extends BoundedFloatFunction<C>> Codec<CubicSpline<C, I>> codec(Codec<I> $$03) {
        record Point<C, I extends BoundedFloatFunction<C>>(float location, CubicSpline<C, I> value, float derivative) {
        }
        MutableObject $$1 = new MutableObject();
        Codec $$22 = RecordCodecBuilder.create($$12 -> $$12.group((App)Codec.FLOAT.fieldOf("location").forGetter(Point::location), (App)Codec.lazyInitialized((Supplier)$$1).fieldOf("value").forGetter(Point::value), (App)Codec.FLOAT.fieldOf("derivative").forGetter(Point::derivative)).apply((Applicative)$$12, ($$0, $$1, $$2) -> new Point((float)$$0, $$1, (float)$$2)));
        Codec $$3 = RecordCodecBuilder.create($$2 -> $$2.group((App)$$03.fieldOf("coordinate").forGetter(Multipoint::coordinate), (App)ExtraCodecs.nonEmptyList($$22.listOf()).fieldOf("points").forGetter($$0 -> IntStream.range(0, $$0.locations.length).mapToObj($$1 -> new Point($$0.locations()[$$1], $$0.values().get($$1), $$0.derivatives()[$$1])).toList())).apply((Applicative)$$2, ($$0, $$1) -> {
            Object $$2 = new float[$$1.size()];
            ImmutableList.Builder $$3 = ImmutableList.builder();
            float[] $$4 = new float[$$1.size()];
            for (int $$5 = 0; $$5 < $$1.size(); ++$$5) {
                Point $$6 = (Point)$$1.get($$5);
                $$2[$$5] = $$6.location();
                $$3.add($$6.value());
                $$4[$$5] = $$6.derivative();
            }
            return Multipoint.create($$0, $$2, $$3.build(), $$4);
        }));
        $$1.setValue((Object)Codec.either((Codec)Codec.FLOAT, (Codec)$$3).xmap($$02 -> (CubicSpline)$$02.map(Constant::new, $$0 -> $$0), $$0 -> {
            Either either;
            if ($$0 instanceof Constant) {
                Constant $$1 = (Constant)$$0;
                either = Either.left((Object)Float.valueOf($$1.value()));
            } else {
                either = Either.right((Object)((Multipoint)$$0));
            }
            return either;
        }));
        return (Codec)$$1.get();
    }

    public static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> constant(float $$0) {
        return new Constant($$0);
    }

    public static <C, I extends BoundedFloatFunction<C>> Builder<C, I> builder(I $$0) {
        return new Builder($$0);
    }

    public static <C, I extends BoundedFloatFunction<C>> Builder<C, I> builder(I $$0, BoundedFloatFunction<Float> $$1) {
        return new Builder($$0, $$1);
    }

    @VisibleForDebug
    public record Constant<C, I extends BoundedFloatFunction<C>>(float value) implements CubicSpline<C, I>
    {
        @Override
        public float apply(C $$0) {
            return this.value;
        }

        @Override
        public String parityString() {
            return String.format(Locale.ROOT, "k=%.3f", Float.valueOf(this.value));
        }

        @Override
        public float minValue() {
            return this.value;
        }

        @Override
        public float maxValue() {
            return this.value;
        }

        @Override
        public CubicSpline<C, I> mapAll(CoordinateVisitor<I> $$0) {
            return this;
        }
    }

    public static final class Builder<C, I extends BoundedFloatFunction<C>> {
        private final I coordinate;
        private final BoundedFloatFunction<Float> valueTransformer;
        private final FloatList locations = new FloatArrayList();
        private final List<CubicSpline<C, I>> values = Lists.newArrayList();
        private final FloatList derivatives = new FloatArrayList();

        protected Builder(I $$0) {
            this($$0, BoundedFloatFunction.IDENTITY);
        }

        protected Builder(I $$0, BoundedFloatFunction<Float> $$1) {
            this.coordinate = $$0;
            this.valueTransformer = $$1;
        }

        public Builder<C, I> addPoint(float $$0, float $$1) {
            return this.addPoint($$0, new Constant(this.valueTransformer.apply(Float.valueOf($$1))), 0.0f);
        }

        public Builder<C, I> addPoint(float $$0, float $$1, float $$2) {
            return this.addPoint($$0, new Constant(this.valueTransformer.apply(Float.valueOf($$1))), $$2);
        }

        public Builder<C, I> addPoint(float $$0, CubicSpline<C, I> $$1) {
            return this.addPoint($$0, $$1, 0.0f);
        }

        private Builder<C, I> addPoint(float $$0, CubicSpline<C, I> $$1, float $$2) {
            if (!this.locations.isEmpty() && $$0 <= this.locations.getFloat(this.locations.size() - 1)) {
                throw new IllegalArgumentException("Please register points in ascending order");
            }
            this.locations.add($$0);
            this.values.add($$1);
            this.derivatives.add($$2);
            return this;
        }

        public CubicSpline<C, I> build() {
            if (this.locations.isEmpty()) {
                throw new IllegalStateException("No elements added");
            }
            return Multipoint.create(this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
        }
    }

    @VisibleForDebug
    public static final class Multipoint<C, I extends BoundedFloatFunction<C>>
    extends Record
    implements CubicSpline<C, I> {
        private final I coordinate;
        final float[] locations;
        private final List<CubicSpline<C, I>> values;
        private final float[] derivatives;
        private final float minValue;
        private final float maxValue;

        public Multipoint(I $$0, float[] $$1, List<CubicSpline<C, I>> $$2, float[] $$3, float $$4, float $$5) {
            Multipoint.validateSizes($$1, $$2, $$3);
            this.coordinate = $$0;
            this.locations = $$1;
            this.values = $$2;
            this.derivatives = $$3;
            this.minValue = $$4;
            this.maxValue = $$5;
        }

        static <C, I extends BoundedFloatFunction<C>> Multipoint<C, I> create(I $$0, float[] $$1, List<CubicSpline<C, I>> $$2, float[] $$3) {
            Multipoint.validateSizes($$1, $$2, $$3);
            int $$4 = $$1.length - 1;
            float $$5 = Float.POSITIVE_INFINITY;
            float $$6 = Float.NEGATIVE_INFINITY;
            float $$7 = $$0.minValue();
            float $$8 = $$0.maxValue();
            if ($$7 < $$1[0]) {
                float $$9 = Multipoint.linearExtend($$7, $$1, $$2.get(0).minValue(), $$3, 0);
                float $$10 = Multipoint.linearExtend($$7, $$1, $$2.get(0).maxValue(), $$3, 0);
                $$5 = Math.min($$5, Math.min($$9, $$10));
                $$6 = Math.max($$6, Math.max($$9, $$10));
            }
            if ($$8 > $$1[$$4]) {
                float $$11 = Multipoint.linearExtend($$8, $$1, $$2.get($$4).minValue(), $$3, $$4);
                float $$12 = Multipoint.linearExtend($$8, $$1, $$2.get($$4).maxValue(), $$3, $$4);
                $$5 = Math.min($$5, Math.min($$11, $$12));
                $$6 = Math.max($$6, Math.max($$11, $$12));
            }
            for (CubicSpline<C, I> $$13 : $$2) {
                $$5 = Math.min($$5, $$13.minValue());
                $$6 = Math.max($$6, $$13.maxValue());
            }
            for (int $$14 = 0; $$14 < $$4; ++$$14) {
                float $$15 = $$1[$$14];
                float $$16 = $$1[$$14 + 1];
                float $$17 = $$16 - $$15;
                CubicSpline<C, I> $$18 = $$2.get($$14);
                CubicSpline<C, I> $$19 = $$2.get($$14 + 1);
                float $$20 = $$18.minValue();
                float $$21 = $$18.maxValue();
                float $$22 = $$19.minValue();
                float $$23 = $$19.maxValue();
                float $$24 = $$3[$$14];
                float $$25 = $$3[$$14 + 1];
                if ($$24 == 0.0f && $$25 == 0.0f) continue;
                float $$26 = $$24 * $$17;
                float $$27 = $$25 * $$17;
                float $$28 = Math.min($$20, $$22);
                float $$29 = Math.max($$21, $$23);
                float $$30 = $$26 - $$23 + $$20;
                float $$31 = $$26 - $$22 + $$21;
                float $$32 = -$$27 + $$22 - $$21;
                float $$33 = -$$27 + $$23 - $$20;
                float $$34 = Math.min($$30, $$32);
                float $$35 = Math.max($$31, $$33);
                $$5 = Math.min($$5, $$28 + 0.25f * $$34);
                $$6 = Math.max($$6, $$29 + 0.25f * $$35);
            }
            return new Multipoint<C, I>($$0, $$1, $$2, $$3, $$5, $$6);
        }

        private static float linearExtend(float $$0, float[] $$1, float $$2, float[] $$3, int $$4) {
            float $$5 = $$3[$$4];
            if ($$5 == 0.0f) {
                return $$2;
            }
            return $$2 + $$5 * ($$0 - $$1[$$4]);
        }

        private static <C, I extends BoundedFloatFunction<C>> void validateSizes(float[] $$0, List<CubicSpline<C, I>> $$1, float[] $$2) {
            if ($$0.length != $$1.size() || $$0.length != $$2.length) {
                throw new IllegalArgumentException("All lengths must be equal, got: " + $$0.length + " " + $$1.size() + " " + $$2.length);
            }
            if ($$0.length == 0) {
                throw new IllegalArgumentException("Cannot create a multipoint spline with no points");
            }
        }

        @Override
        public float apply(C $$0) {
            float $$1 = this.coordinate.apply($$0);
            int $$2 = Multipoint.findIntervalStart(this.locations, $$1);
            int $$3 = this.locations.length - 1;
            if ($$2 < 0) {
                return Multipoint.linearExtend($$1, this.locations, this.values.get(0).apply($$0), this.derivatives, 0);
            }
            if ($$2 == $$3) {
                return Multipoint.linearExtend($$1, this.locations, this.values.get($$3).apply($$0), this.derivatives, $$3);
            }
            float $$4 = this.locations[$$2];
            float $$5 = this.locations[$$2 + 1];
            float $$6 = ($$1 - $$4) / ($$5 - $$4);
            BoundedFloatFunction $$7 = this.values.get($$2);
            BoundedFloatFunction $$8 = this.values.get($$2 + 1);
            float $$9 = this.derivatives[$$2];
            float $$10 = this.derivatives[$$2 + 1];
            float $$11 = $$7.apply($$0);
            float $$12 = $$8.apply($$0);
            float $$13 = $$9 * ($$5 - $$4) - ($$12 - $$11);
            float $$14 = -$$10 * ($$5 - $$4) + ($$12 - $$11);
            float $$15 = Mth.lerp($$6, $$11, $$12) + $$6 * (1.0f - $$6) * Mth.lerp($$6, $$13, $$14);
            return $$15;
        }

        private static int findIntervalStart(float[] $$0, float $$1) {
            return Mth.binarySearch(0, $$0.length, $$2 -> $$1 < $$0[$$2]) - 1;
        }

        @Override
        @VisibleForTesting
        public String parityString() {
            return "Spline{coordinate=" + String.valueOf(this.coordinate) + ", locations=" + this.toString(this.locations) + ", derivatives=" + this.toString(this.derivatives) + ", values=" + this.values.stream().map(CubicSpline::parityString).collect(Collectors.joining(", ", "[", "]")) + "}";
        }

        private String toString(float[] $$02) {
            return "[" + IntStream.range(0, $$02.length).mapToDouble($$1 -> $$02[$$1]).mapToObj($$0 -> String.format(Locale.ROOT, "%.3f", $$0)).collect(Collectors.joining(", ")) + "]";
        }

        @Override
        public CubicSpline<C, I> mapAll(CoordinateVisitor<I> $$0) {
            return Multipoint.create((BoundedFloatFunction)$$0.visit(this.coordinate), this.locations, this.values().stream().map($$1 -> $$1.mapAll($$0)).toList(), this.derivatives);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Multipoint.class, "coordinate;locations;values;derivatives;minValue;maxValue", "coordinate", "locations", "values", "derivatives", "minValue", "maxValue"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Multipoint.class, "coordinate;locations;values;derivatives;minValue;maxValue", "coordinate", "locations", "values", "derivatives", "minValue", "maxValue"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Multipoint.class, "coordinate;locations;values;derivatives;minValue;maxValue", "coordinate", "locations", "values", "derivatives", "minValue", "maxValue"}, this, $$0);
        }

        public I coordinate() {
            return this.coordinate;
        }

        public float[] locations() {
            return this.locations;
        }

        public List<CubicSpline<C, I>> values() {
            return this.values;
        }

        public float[] derivatives() {
            return this.derivatives;
        }

        @Override
        public float minValue() {
            return this.minValue;
        }

        @Override
        public float maxValue() {
            return this.maxValue;
        }
    }

    public static interface CoordinateVisitor<I> {
        public I visit(I var1);
    }
}

