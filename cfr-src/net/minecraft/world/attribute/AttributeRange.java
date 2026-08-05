/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.world.attribute;

import com.mojang.serialization.DataResult;
import net.minecraft.util.Mth;

public interface AttributeRange<Value> {
    public static final AttributeRange<Float> UNIT_FLOAT = AttributeRange.ofFloat(0.0f, 1.0f);
    public static final AttributeRange<Float> NON_NEGATIVE_FLOAT = AttributeRange.ofFloat(0.0f, Float.POSITIVE_INFINITY);

    public static <Value> AttributeRange<Value> any() {
        return new AttributeRange<Value>(){

            @Override
            public DataResult<Value> validate(Value $$0) {
                return DataResult.success($$0);
            }

            @Override
            public Value sanitize(Value $$0) {
                return $$0;
            }
        };
    }

    public static AttributeRange<Float> ofFloat(final float $$0, final float $$1) {
        return new AttributeRange<Float>(){

            @Override
            public DataResult<Float> validate(Float $$02) {
                if ($$02.floatValue() >= $$0 && $$02.floatValue() <= $$1) {
                    return DataResult.success((Object)$$02);
                }
                return DataResult.error(() -> $$02 + " is not in range [" + $$0 + "; " + $$1 + "]");
            }

            @Override
            public Float sanitize(Float $$02) {
                if ($$02.floatValue() >= $$0 && $$02.floatValue() <= $$1) {
                    return $$02;
                }
                return Float.valueOf(Mth.clamp($$02.floatValue(), $$0, $$1));
            }
        };
    }

    public DataResult<Value> validate(Value var1);

    public Value sanitize(Value var1);
}

