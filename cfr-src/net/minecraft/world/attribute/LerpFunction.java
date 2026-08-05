/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.attribute;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public interface LerpFunction<T> {
    public static LerpFunction<Float> ofFloat() {
        return Mth::lerp;
    }

    public static LerpFunction<Float> ofDegrees(float $$0) {
        return ($$1, $$2, $$3) -> {
            float $$4 = Mth.wrapDegrees($$3.floatValue() - $$2.floatValue());
            if (Math.abs($$4) >= $$0) {
                return $$3;
            }
            return Float.valueOf($$2.floatValue() + $$1 * $$4);
        };
    }

    public static <T> LerpFunction<T> ofConstant() {
        return ($$0, $$1, $$2) -> $$1;
    }

    public static <T> LerpFunction<T> ofStep(float $$0) {
        return ($$1, $$2, $$3) -> $$1 >= $$0 ? $$3 : $$2;
    }

    public static LerpFunction<Integer> ofColor() {
        return ARGB::srgbLerp;
    }

    public T apply(float var1, T var2, T var3);
}

