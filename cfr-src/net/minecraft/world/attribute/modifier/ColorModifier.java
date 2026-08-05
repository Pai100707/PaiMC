/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public interface ColorModifier<Argument>
extends AttributeModifier<Integer, Argument> {
    public static final ColorModifier<Integer> ALPHA_BLEND = new ColorModifier<Integer>(){

        @Override
        public Integer apply(Integer $$0, Integer $$1) {
            return ARGB.alphaBlend($$0, $$1);
        }

        @Override
        public Codec<Integer> argumentCodec(EnvironmentAttribute<Integer> $$0) {
            return ExtraCodecs.STRING_ARGB_COLOR;
        }

        @Override
        public LerpFunction<Integer> argumentKeyframeLerp(EnvironmentAttribute<Integer> $$0) {
            return LerpFunction.ofColor();
        }

        @Override
        public /* synthetic */ Object apply(Object object, Object object2) {
            return this.apply((Integer)object, (Integer)object2);
        }
    };
    public static final ColorModifier<Integer> ADD = ARGB::addRgb;
    public static final ColorModifier<Integer> SUBTRACT = ARGB::subtractRgb;
    public static final ColorModifier<Integer> MULTIPLY_RGB = ARGB::multiply;
    public static final ColorModifier<Integer> MULTIPLY_ARGB = ARGB::multiply;
    public static final ColorModifier<BlendToGray> BLEND_TO_GRAY = new ColorModifier<BlendToGray>(){

        @Override
        public Integer apply(Integer $$0, BlendToGray $$1) {
            int $$2 = ARGB.scaleRGB(ARGB.greyscale($$0), $$1.brightness);
            return ARGB.srgbLerp($$1.factor, $$0, $$2);
        }

        @Override
        public Codec<BlendToGray> argumentCodec(EnvironmentAttribute<Integer> $$0) {
            return BlendToGray.CODEC;
        }

        @Override
        public LerpFunction<BlendToGray> argumentKeyframeLerp(EnvironmentAttribute<Integer> $$02) {
            return ($$0, $$1, $$2) -> new BlendToGray(Mth.lerp($$0, $$1.brightness, $$2.brightness), Mth.lerp($$0, $$1.factor, $$2.factor));
        }

        @Override
        public /* synthetic */ Object apply(Object object, Object object2) {
            return this.apply((Integer)object, (BlendToGray)object2);
        }
    };

    @FunctionalInterface
    public static interface RgbModifier
    extends ColorModifier<Integer> {
        @Override
        default public Codec<Integer> argumentCodec(EnvironmentAttribute<Integer> $$0) {
            return ExtraCodecs.STRING_RGB_COLOR;
        }

        @Override
        default public LerpFunction<Integer> argumentKeyframeLerp(EnvironmentAttribute<Integer> $$0) {
            return LerpFunction.ofColor();
        }
    }

    @FunctionalInterface
    public static interface ArgbModifier
    extends ColorModifier<Integer> {
        @Override
        default public Codec<Integer> argumentCodec(EnvironmentAttribute<Integer> $$02) {
            return Codec.either(ExtraCodecs.STRING_ARGB_COLOR, ExtraCodecs.RGB_COLOR_CODEC).xmap(Either::unwrap, $$0 -> ARGB.alpha($$0) == 255 ? Either.right((Object)$$0) : Either.left((Object)$$0));
        }

        @Override
        default public LerpFunction<Integer> argumentKeyframeLerp(EnvironmentAttribute<Integer> $$0) {
            return LerpFunction.ofColor();
        }
    }

    public static final class BlendToGray
    extends Record {
        final float brightness;
        final float factor;
        public static final Codec<BlendToGray> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("brightness").forGetter(BlendToGray::brightness), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("factor").forGetter(BlendToGray::factor)).apply((Applicative)$$0, BlendToGray::new));

        public BlendToGray(float $$0, float $$1) {
            this.brightness = $$0;
            this.factor = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BlendToGray.class, "brightness;factor", "brightness", "factor"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BlendToGray.class, "brightness;factor", "brightness", "factor"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BlendToGray.class, "brightness;factor", "brightness", "factor"}, this, $$0);
        }

        public float brightness() {
            return this.brightness;
        }

        public float factor() {
            return this.factor;
        }
    }
}

