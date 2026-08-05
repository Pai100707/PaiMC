/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.attribute.modifier.FloatWithAlpha;

public interface FloatModifier<Argument>
extends AttributeModifier<Float, Argument> {
    public static final FloatModifier<FloatWithAlpha> ALPHA_BLEND = new FloatModifier<FloatWithAlpha>(){

        @Override
        public Float apply(Float $$0, FloatWithAlpha $$1) {
            return Float.valueOf(Mth.lerp($$1.alpha(), $$0.floatValue(), $$1.value()));
        }

        @Override
        public Codec<FloatWithAlpha> argumentCodec(EnvironmentAttribute<Float> $$0) {
            return FloatWithAlpha.CODEC;
        }

        @Override
        public LerpFunction<FloatWithAlpha> argumentKeyframeLerp(EnvironmentAttribute<Float> $$02) {
            return ($$0, $$1, $$2) -> new FloatWithAlpha(Mth.lerp($$0, $$1.value(), $$2.value()), Mth.lerp($$0, $$1.alpha(), $$2.alpha()));
        }

        @Override
        public /* synthetic */ Object apply(Object object, Object object2) {
            return this.apply((Float)object, (FloatWithAlpha)object2);
        }
    };
    public static final FloatModifier<Float> ADD = Float::sum;
    public static final FloatModifier<Float> SUBTRACT = ($$0, $$1) -> Float.valueOf($$0.floatValue() - $$1.floatValue());
    public static final FloatModifier<Float> MULTIPLY = ($$0, $$1) -> Float.valueOf($$0.floatValue() * $$1.floatValue());
    public static final FloatModifier<Float> MINIMUM = Math::min;
    public static final FloatModifier<Float> MAXIMUM = Math::max;

    @FunctionalInterface
    public static interface Simple
    extends FloatModifier<Float> {
        @Override
        default public Codec<Float> argumentCodec(EnvironmentAttribute<Float> $$0) {
            return Codec.FLOAT;
        }

        @Override
        default public LerpFunction<Float> argumentKeyframeLerp(EnvironmentAttribute<Float> $$0) {
            return LerpFunction.ofFloat();
        }
    }
}

