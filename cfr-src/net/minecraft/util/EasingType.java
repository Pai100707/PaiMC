/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.minecraft.util.Ease;
import net.minecraft.util.ExtraCodecs;

public interface EasingType {
    public static final ExtraCodecs.LateBoundIdMapper<String, EasingType> SIMPLE_REGISTRY = new ExtraCodecs.LateBoundIdMapper();
    public static final Codec<EasingType> CODEC = Codec.either(SIMPLE_REGISTRY.codec((Codec<String>)Codec.STRING), CubicBezier.CODEC).xmap(Either::unwrap, $$0 -> {
        Either either;
        if ($$0 instanceof CubicBezier) {
            CubicBezier $$1 = (CubicBezier)$$0;
            either = Either.right((Object)$$1);
        } else {
            either = Either.left((Object)$$0);
        }
        return either;
    });
    public static final EasingType CONSTANT = EasingType.registerSimple("constant", $$0 -> 0.0f);
    public static final EasingType LINEAR = EasingType.registerSimple("linear", $$0 -> $$0);
    public static final EasingType IN_BACK = EasingType.registerSimple("in_back", Ease::inBack);
    public static final EasingType IN_BOUNCE = EasingType.registerSimple("in_bounce", Ease::inBounce);
    public static final EasingType IN_CIRC = EasingType.registerSimple("in_circ", Ease::inCirc);
    public static final EasingType IN_CUBIC = EasingType.registerSimple("in_cubic", Ease::inCubic);
    public static final EasingType IN_ELASTIC = EasingType.registerSimple("in_elastic", Ease::inElastic);
    public static final EasingType IN_EXPO = EasingType.registerSimple("in_expo", Ease::inExpo);
    public static final EasingType IN_QUAD = EasingType.registerSimple("in_quad", Ease::inQuad);
    public static final EasingType IN_QUART = EasingType.registerSimple("in_quart", Ease::inQuart);
    public static final EasingType IN_QUINT = EasingType.registerSimple("in_quint", Ease::inQuint);
    public static final EasingType IN_SINE = EasingType.registerSimple("in_sine", Ease::inSine);
    public static final EasingType IN_OUT_BACK = EasingType.registerSimple("in_out_back", Ease::inOutBack);
    public static final EasingType IN_OUT_BOUNCE = EasingType.registerSimple("in_out_bounce", Ease::inOutBounce);
    public static final EasingType IN_OUT_CIRC = EasingType.registerSimple("in_out_circ", Ease::inOutCirc);
    public static final EasingType IN_OUT_CUBIC = EasingType.registerSimple("in_out_cubic", Ease::inOutCubic);
    public static final EasingType IN_OUT_ELASTIC = EasingType.registerSimple("in_out_elastic", Ease::inOutElastic);
    public static final EasingType IN_OUT_EXPO = EasingType.registerSimple("in_out_expo", Ease::inOutExpo);
    public static final EasingType IN_OUT_QUAD = EasingType.registerSimple("in_out_quad", Ease::inOutQuad);
    public static final EasingType IN_OUT_QUART = EasingType.registerSimple("in_out_quart", Ease::inOutQuart);
    public static final EasingType IN_OUT_QUINT = EasingType.registerSimple("in_out_quint", Ease::inOutQuint);
    public static final EasingType IN_OUT_SINE = EasingType.registerSimple("in_out_sine", Ease::inOutSine);
    public static final EasingType OUT_BACK = EasingType.registerSimple("out_back", Ease::outBack);
    public static final EasingType OUT_BOUNCE = EasingType.registerSimple("out_bounce", Ease::outBounce);
    public static final EasingType OUT_CIRC = EasingType.registerSimple("out_circ", Ease::outCirc);
    public static final EasingType OUT_CUBIC = EasingType.registerSimple("out_cubic", Ease::outCubic);
    public static final EasingType OUT_ELASTIC = EasingType.registerSimple("out_elastic", Ease::outElastic);
    public static final EasingType OUT_EXPO = EasingType.registerSimple("out_expo", Ease::outExpo);
    public static final EasingType OUT_QUAD = EasingType.registerSimple("out_quad", Ease::outQuad);
    public static final EasingType OUT_QUART = EasingType.registerSimple("out_quart", Ease::outQuart);
    public static final EasingType OUT_QUINT = EasingType.registerSimple("out_quint", Ease::outQuint);
    public static final EasingType OUT_SINE = EasingType.registerSimple("out_sine", Ease::outSine);

    public static EasingType registerSimple(String $$0, EasingType $$1) {
        SIMPLE_REGISTRY.put($$0, $$1);
        return $$1;
    }

    public static EasingType cubicBezier(float $$0, float $$1, float $$2, float $$3) {
        return new CubicBezier(new CubicBezierControls($$0, $$1, $$2, $$3));
    }

    public static EasingType symmetricCubicBezier(float $$0, float $$1) {
        return EasingType.cubicBezier($$0, $$1, 1.0f - $$0, 1.0f - $$1);
    }

    public float apply(float var1);

    public static final class CubicBezier
    implements EasingType {
        public static final Codec<CubicBezier> CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)CubicBezierControls.CODEC.fieldOf("cubic_bezier").forGetter($$0 -> $$0.controls)).apply((Applicative)$$02, CubicBezier::new));
        private static final int NEWTON_RAPHSON_ITERATIONS = 4;
        private final CubicBezierControls controls;
        private final CubicCurve xCurve;
        private final CubicCurve yCurve;

        public CubicBezier(CubicBezierControls $$0) {
            this.controls = $$0;
            this.xCurve = CubicBezier.curveFromControls($$0.x1, $$0.x2);
            this.yCurve = CubicBezier.curveFromControls($$0.y1, $$0.y2);
        }

        private static CubicCurve curveFromControls(float $$0, float $$1) {
            return new CubicCurve(3.0f * $$0 - 3.0f * $$1 + 1.0f, -6.0f * $$0 + 3.0f * $$1, 3.0f * $$0);
        }

        @Override
        public float apply(float $$0) {
            float $$3;
            float $$1 = $$0;
            for (int $$2 = 0; $$2 < 4 && !(($$3 = this.xCurve.sampleGradient($$1)) < 1.0E-5f); ++$$2) {
                float $$4 = this.xCurve.sample($$1) - $$0;
                $$1 -= $$4 / $$3;
            }
            return this.yCurve.sample($$1);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object $$0) {
            if (!($$0 instanceof CubicBezier)) return false;
            CubicBezier $$1 = (CubicBezier)$$0;
            if (!this.controls.equals($$1.controls)) return false;
            return true;
        }

        public int hashCode() {
            return this.controls.hashCode();
        }

        public String toString() {
            return "CubicBezier(" + this.controls.x1 + ", " + this.controls.y1 + ", " + this.controls.x2 + ", " + this.controls.y2 + ")";
        }

        record CubicCurve(float a, float b, float c) {
            public float sample(float $$0) {
                return ((this.a * $$0 + this.b) * $$0 + this.c) * $$0;
            }

            public float sampleGradient(float $$0) {
                return (3.0f * this.a * $$0 + 2.0f * this.b) * $$0 + this.c;
            }
        }
    }

    public static final class CubicBezierControls
    extends Record {
        final float x1;
        final float y1;
        final float x2;
        final float y2;
        public static final Codec<CubicBezierControls> CODEC = Codec.FLOAT.listOf(4, 4).xmap($$0 -> new CubicBezierControls(((Float)$$0.get(0)).floatValue(), ((Float)$$0.get(1)).floatValue(), ((Float)$$0.get(2)).floatValue(), ((Float)$$0.get(3)).floatValue()), $$0 -> List.of(Float.valueOf($$0.x1), Float.valueOf($$0.y1), Float.valueOf($$0.x2), Float.valueOf($$0.y2))).validate(CubicBezierControls::validate);

        public CubicBezierControls(float $$0, float $$1, float $$2, float $$3) {
            this.x1 = $$0;
            this.y1 = $$1;
            this.x2 = $$2;
            this.y2 = $$3;
        }

        private DataResult<CubicBezierControls> validate() {
            if (this.x1 < 0.0f || this.x1 > 1.0f) {
                return DataResult.error(() -> "x1 must be in range [0; 1]");
            }
            if (this.x2 < 0.0f || this.x2 > 1.0f) {
                return DataResult.error(() -> "x2 must be in range [0; 1]");
            }
            return DataResult.success((Object)this);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CubicBezierControls.class, "x1;y1;x2;y2", "x1", "y1", "x2", "y2"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CubicBezierControls.class, "x1;y1;x2;y2", "x1", "y1", "x2", "y2"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CubicBezierControls.class, "x1;y1;x2;y2", "x1", "y1", "x2", "y2"}, this, $$0);
        }

        public float x1() {
            return this.x1;
        }

        public float y1() {
            return this.y1;
        }

        public float x2() {
            return this.x2;
        }

        public float y2() {
            return this.y2;
        }
    }
}

