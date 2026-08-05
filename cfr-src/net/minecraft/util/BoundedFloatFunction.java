/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.floats.Float2FloatFunction
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.function.Function;

public interface BoundedFloatFunction<C> {
    public static final BoundedFloatFunction<Float> IDENTITY = BoundedFloatFunction.createUnlimited($$0 -> $$0);

    public float apply(C var1);

    public float minValue();

    public float maxValue();

    public static BoundedFloatFunction<Float> createUnlimited(final Float2FloatFunction $$0) {
        return new BoundedFloatFunction<Float>(){

            @Override
            public float apply(Float $$02) {
                return ((Float)$$0.apply((Object)$$02)).floatValue();
            }

            @Override
            public float minValue() {
                return Float.NEGATIVE_INFINITY;
            }

            @Override
            public float maxValue() {
                return Float.POSITIVE_INFINITY;
            }
        };
    }

    default public <C2> BoundedFloatFunction<C2> comap(final Function<C2, C> $$0) {
        final BoundedFloatFunction $$1 = this;
        return new BoundedFloatFunction<C2>(this){

            @Override
            public float apply(C2 $$02) {
                return $$1.apply($$0.apply($$02));
            }

            @Override
            public float minValue() {
                return $$1.minValue();
            }

            @Override
            public float maxValue() {
                return $$1.maxValue();
            }
        };
    }
}

