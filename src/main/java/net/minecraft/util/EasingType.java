package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public interface EasingType {
   net.minecraft.util.ExtraCodecs.LateBoundIdMapper<String, net.minecraft.util.EasingType> SIMPLE_REGISTRY = new net.minecraft.util.ExtraCodecs.LateBoundIdMapper<>();
   Codec<net.minecraft.util.EasingType> CODEC = Codec.either(SIMPLE_REGISTRY.codec(Codec.STRING), net.minecraft.util.EasingType.CubicBezier.CODEC)
      .xmap(Either::unwrap, $$0 -> $$0 instanceof net.minecraft.util.EasingType.CubicBezier $$1 ? Either.right($$1) : Either.left($$0));
   net.minecraft.util.EasingType CONSTANT = registerSimple("constant", $$0 -> 0.0F);
   net.minecraft.util.EasingType LINEAR = registerSimple("linear", $$0 -> $$0);
   net.minecraft.util.EasingType IN_BACK = registerSimple("in_back", net.minecraft.util.Ease::inBack);
   net.minecraft.util.EasingType IN_BOUNCE = registerSimple("in_bounce", net.minecraft.util.Ease::inBounce);
   net.minecraft.util.EasingType IN_CIRC = registerSimple("in_circ", net.minecraft.util.Ease::inCirc);
   net.minecraft.util.EasingType IN_CUBIC = registerSimple("in_cubic", net.minecraft.util.Ease::inCubic);
   net.minecraft.util.EasingType IN_ELASTIC = registerSimple("in_elastic", net.minecraft.util.Ease::inElastic);
   net.minecraft.util.EasingType IN_EXPO = registerSimple("in_expo", net.minecraft.util.Ease::inExpo);
   net.minecraft.util.EasingType IN_QUAD = registerSimple("in_quad", net.minecraft.util.Ease::inQuad);
   net.minecraft.util.EasingType IN_QUART = registerSimple("in_quart", net.minecraft.util.Ease::inQuart);
   net.minecraft.util.EasingType IN_QUINT = registerSimple("in_quint", net.minecraft.util.Ease::inQuint);
   net.minecraft.util.EasingType IN_SINE = registerSimple("in_sine", net.minecraft.util.Ease::inSine);
   net.minecraft.util.EasingType IN_OUT_BACK = registerSimple("in_out_back", net.minecraft.util.Ease::inOutBack);
   net.minecraft.util.EasingType IN_OUT_BOUNCE = registerSimple("in_out_bounce", net.minecraft.util.Ease::inOutBounce);
   net.minecraft.util.EasingType IN_OUT_CIRC = registerSimple("in_out_circ", net.minecraft.util.Ease::inOutCirc);
   net.minecraft.util.EasingType IN_OUT_CUBIC = registerSimple("in_out_cubic", net.minecraft.util.Ease::inOutCubic);
   net.minecraft.util.EasingType IN_OUT_ELASTIC = registerSimple("in_out_elastic", net.minecraft.util.Ease::inOutElastic);
   net.minecraft.util.EasingType IN_OUT_EXPO = registerSimple("in_out_expo", net.minecraft.util.Ease::inOutExpo);
   net.minecraft.util.EasingType IN_OUT_QUAD = registerSimple("in_out_quad", net.minecraft.util.Ease::inOutQuad);
   net.minecraft.util.EasingType IN_OUT_QUART = registerSimple("in_out_quart", net.minecraft.util.Ease::inOutQuart);
   net.minecraft.util.EasingType IN_OUT_QUINT = registerSimple("in_out_quint", net.minecraft.util.Ease::inOutQuint);
   net.minecraft.util.EasingType IN_OUT_SINE = registerSimple("in_out_sine", net.minecraft.util.Ease::inOutSine);
   net.minecraft.util.EasingType OUT_BACK = registerSimple("out_back", net.minecraft.util.Ease::outBack);
   net.minecraft.util.EasingType OUT_BOUNCE = registerSimple("out_bounce", net.minecraft.util.Ease::outBounce);
   net.minecraft.util.EasingType OUT_CIRC = registerSimple("out_circ", net.minecraft.util.Ease::outCirc);
   net.minecraft.util.EasingType OUT_CUBIC = registerSimple("out_cubic", net.minecraft.util.Ease::outCubic);
   net.minecraft.util.EasingType OUT_ELASTIC = registerSimple("out_elastic", net.minecraft.util.Ease::outElastic);
   net.minecraft.util.EasingType OUT_EXPO = registerSimple("out_expo", net.minecraft.util.Ease::outExpo);
   net.minecraft.util.EasingType OUT_QUAD = registerSimple("out_quad", net.minecraft.util.Ease::outQuad);
   net.minecraft.util.EasingType OUT_QUART = registerSimple("out_quart", net.minecraft.util.Ease::outQuart);
   net.minecraft.util.EasingType OUT_QUINT = registerSimple("out_quint", net.minecraft.util.Ease::outQuint);
   net.minecraft.util.EasingType OUT_SINE = registerSimple("out_sine", net.minecraft.util.Ease::outSine);

   static net.minecraft.util.EasingType registerSimple(String $$0, net.minecraft.util.EasingType $$1) {
      SIMPLE_REGISTRY.put($$0, $$1);
      return $$1;
   }

   static net.minecraft.util.EasingType cubicBezier(float $$0, float $$1, float $$2, float $$3) {
      return new net.minecraft.util.EasingType.CubicBezier(new net.minecraft.util.EasingType.CubicBezierControls($$0, $$1, $$2, $$3));
   }

   static net.minecraft.util.EasingType symmetricCubicBezier(float $$0, float $$1) {
      return cubicBezier($$0, $$1, 1.0F - $$0, 1.0F - $$1);
   }

   float apply(float var1);

   public static final class CubicBezier implements net.minecraft.util.EasingType {
      public static final Codec<net.minecraft.util.EasingType.CubicBezier> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(net.minecraft.util.EasingType.CubicBezierControls.CODEC.fieldOf("cubic_bezier").forGetter($$0x -> $$0x.controls))
            .apply($$0, net.minecraft.util.EasingType.CubicBezier::new)
      );
      private static final int NEWTON_RAPHSON_ITERATIONS = 4;
      private final net.minecraft.util.EasingType.CubicBezierControls controls;
      private final net.minecraft.util.EasingType.CubicBezier.CubicCurve xCurve;
      private final net.minecraft.util.EasingType.CubicBezier.CubicCurve yCurve;

      public CubicBezier(net.minecraft.util.EasingType.CubicBezierControls $$0) {
         this.controls = $$0;
         this.xCurve = curveFromControls($$0.x1, $$0.x2);
         this.yCurve = curveFromControls($$0.y1, $$0.y2);
      }

      private static net.minecraft.util.EasingType.CubicBezier.CubicCurve curveFromControls(float $$0, float $$1) {
         return new net.minecraft.util.EasingType.CubicBezier.CubicCurve(3.0F * $$0 - 3.0F * $$1 + 1.0F, -6.0F * $$0 + 3.0F * $$1, 3.0F * $$0);
      }

      @Override
      public float apply(float $$0) {
         float $$1 = $$0;

         for (int $$2 = 0; $$2 < 4; $$2++) {
            float $$3 = this.xCurve.sampleGradient($$1);
            if ($$3 < 1.0E-5F) {
               break;
            }

            float $$4 = this.xCurve.sample($$1) - $$0;
            $$1 -= $$4 / $$3;
         }

         return this.yCurve.sample($$1);
      }

      @Override
      public boolean equals(Object $$0) {
         return $$0 instanceof net.minecraft.util.EasingType.CubicBezier $$1 && this.controls.equals($$1.controls);
      }

      @Override
      public int hashCode() {
         return this.controls.hashCode();
      }

      @Override
      public String toString() {
         return "CubicBezier(" + this.controls.x1 + ", " + this.controls.y1 + ", " + this.controls.x2 + ", " + this.controls.y2 + ")";
      }

      record CubicCurve(float a, float b, float c) {
         public float sample(float $$0) {
            return ((this.a * $$0 + this.b) * $$0 + this.c) * $$0;
         }

         public float sampleGradient(float $$0) {
            return (3.0F * this.a * $$0 + 2.0F * this.b) * $$0 + this.c;
         }
      }
   }

   public record CubicBezierControls(float x1, float y1, float x2, float y2) {
      public static final Codec<net.minecraft.util.EasingType.CubicBezierControls> CODEC = Codec.FLOAT
         .listOf(4, 4)
         .xmap(
            $$0 -> new net.minecraft.util.EasingType.CubicBezierControls((Float)$$0.get(0), (Float)$$0.get(1), (Float)$$0.get(2), (Float)$$0.get(3)),
            $$0 -> List.of($$0.x1, $$0.y1, $$0.x2, $$0.y2)
         )
         .validate(net.minecraft.util.EasingType.CubicBezierControls::validate);

      private DataResult<net.minecraft.util.EasingType.CubicBezierControls> validate() {
         if (this.x1 < 0.0F || this.x1 > 1.0F) {
            return DataResult.error(() -> "x1 must be in range [0; 1]");
         } else {
            return !(this.x2 < 0.0F) && !(this.x2 > 1.0F) ? DataResult.success(this) : DataResult.error(() -> "x2 must be in range [0; 1]");
         }
      }
   }
}
