package net.minecraft.data.worldgen;

import net.minecraft.util.BoundedFloatFunction;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.CubicSpline.Builder;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class TerrainProvider {
   private static final float DEEP_OCEAN_CONTINENTALNESS = -0.51F;
   private static final float OCEAN_CONTINENTALNESS = -0.4F;
   private static final float PLAINS_CONTINENTALNESS = 0.1F;
   private static final float BEACH_CONTINENTALNESS = -0.15F;
   private static final BoundedFloatFunction<Float> NO_TRANSFORM = BoundedFloatFunction.IDENTITY;
   private static final BoundedFloatFunction<Float> AMPLIFIED_OFFSET = BoundedFloatFunction.createUnlimited($$0 -> $$0 < 0.0F ? $$0 : $$0 * 2.0F);
   private static final BoundedFloatFunction<Float> AMPLIFIED_FACTOR = BoundedFloatFunction.createUnlimited($$0 -> 1.25F - 6.25F / ($$0 + 5.0F));
   private static final BoundedFloatFunction<Float> AMPLIFIED_JAGGEDNESS = BoundedFloatFunction.createUnlimited($$0 -> $$0 * 2.0F);

   public static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> overworldOffset(I $$0, I $$1, I $$2, boolean $$3) {
      BoundedFloatFunction<Float> $$4 = $$3 ? AMPLIFIED_OFFSET : NO_TRANSFORM;
      CubicSpline<C, I> $$5 = buildErosionOffsetSpline($$1, $$2, -0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false, $$4);
      CubicSpline<C, I> $$6 = buildErosionOffsetSpline($$1, $$2, -0.1F, 0.03F, 0.1F, 0.1F, 0.01F, -0.03F, false, false, $$4);
      CubicSpline<C, I> $$7 = buildErosionOffsetSpline($$1, $$2, -0.1F, 0.03F, 0.1F, 0.7F, 0.01F, -0.03F, true, true, $$4);
      CubicSpline<C, I> $$8 = buildErosionOffsetSpline($$1, $$2, -0.05F, 0.03F, 0.1F, 1.0F, 0.01F, 0.01F, true, true, $$4);
      return CubicSpline.builder($$0, $$4)
         .addPoint(-1.1F, 0.044F)
         .addPoint(-1.02F, -0.2222F)
         .addPoint(-0.51F, -0.2222F)
         .addPoint(-0.44F, -0.12F)
         .addPoint(-0.18F, -0.12F)
         .addPoint(-0.16F, $$5)
         .addPoint(-0.15F, $$5)
         .addPoint(-0.1F, $$6)
         .addPoint(0.25F, $$7)
         .addPoint(1.0F, $$8)
         .build();
   }

   public static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> overworldFactor(I $$0, I $$1, I $$2, I $$3, boolean $$4) {
      BoundedFloatFunction<Float> $$5 = $$4 ? AMPLIFIED_FACTOR : NO_TRANSFORM;
      return CubicSpline.builder($$0, NO_TRANSFORM)
         .addPoint(-0.19F, 3.95F)
         .addPoint(-0.15F, getErosionFactor($$1, $$2, $$3, 6.25F, true, NO_TRANSFORM))
         .addPoint(-0.1F, getErosionFactor($$1, $$2, $$3, 5.47F, true, $$5))
         .addPoint(0.03F, getErosionFactor($$1, $$2, $$3, 5.08F, true, $$5))
         .addPoint(0.06F, getErosionFactor($$1, $$2, $$3, 4.69F, false, $$5))
         .build();
   }

   public static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> overworldJaggedness(I $$0, I $$1, I $$2, I $$3, boolean $$4) {
      BoundedFloatFunction<Float> $$5 = $$4 ? AMPLIFIED_JAGGEDNESS : NO_TRANSFORM;
      float $$6 = 0.65F;
      return CubicSpline.builder($$0, $$5)
         .addPoint(-0.11F, 0.0F)
         .addPoint(0.03F, buildErosionJaggednessSpline($$1, $$2, $$3, 1.0F, 0.5F, 0.0F, 0.0F, $$5))
         .addPoint(0.65F, buildErosionJaggednessSpline($$1, $$2, $$3, 1.0F, 1.0F, 1.0F, 0.0F, $$5))
         .build();
   }

   private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> buildErosionJaggednessSpline(
      I $$0, I $$1, I $$2, float $$3, float $$4, float $$5, float $$6, BoundedFloatFunction<Float> $$7
   ) {
      float $$8 = -0.5775F;
      CubicSpline<C, I> $$9 = buildRidgeJaggednessSpline($$1, $$2, $$3, $$5, $$7);
      CubicSpline<C, I> $$10 = buildRidgeJaggednessSpline($$1, $$2, $$4, $$6, $$7);
      return CubicSpline.builder($$0, $$7).addPoint(-1.0F, $$9).addPoint(-0.78F, $$10).addPoint(-0.5775F, $$10).addPoint(-0.375F, 0.0F).build();
   }

   private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> buildRidgeJaggednessSpline(
      I $$0, I $$1, float $$2, float $$3, BoundedFloatFunction<Float> $$4
   ) {
      float $$5 = NoiseRouterData.peaksAndValleys(0.4F);
      float $$6 = NoiseRouterData.peaksAndValleys(0.56666666F);
      float $$7 = ($$5 + $$6) / 2.0F;
      Builder<C, I> $$8 = CubicSpline.builder($$1, $$4);
      $$8.addPoint($$5, 0.0F);
      if ($$3 > 0.0F) {
         $$8.addPoint($$7, buildWeirdnessJaggednessSpline($$0, $$3, $$4));
      } else {
         $$8.addPoint($$7, 0.0F);
      }

      if ($$2 > 0.0F) {
         $$8.addPoint(1.0F, buildWeirdnessJaggednessSpline($$0, $$2, $$4));
      } else {
         $$8.addPoint(1.0F, 0.0F);
      }

      return $$8.build();
   }

   private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> buildWeirdnessJaggednessSpline(I $$0, float $$1, BoundedFloatFunction<Float> $$2) {
      float $$3 = 0.63F * $$1;
      float $$4 = 0.3F * $$1;
      return CubicSpline.builder($$0, $$2).addPoint(-0.01F, $$3).addPoint(0.01F, $$4).build();
   }

   private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> getErosionFactor(
      I $$0, I $$1, I $$2, float $$3, boolean $$4, BoundedFloatFunction<Float> $$5
   ) {
      CubicSpline<C, I> $$6 = CubicSpline.builder($$1, $$5).addPoint(-0.2F, 6.3F).addPoint(0.2F, $$3).build();
      Builder<C, I> $$7 = CubicSpline.builder($$0, $$5)
         .addPoint(-0.6F, $$6)
         .addPoint(-0.5F, CubicSpline.builder($$1, $$5).addPoint(-0.05F, 6.3F).addPoint(0.05F, 2.67F).build())
         .addPoint(-0.35F, $$6)
         .addPoint(-0.25F, $$6)
         .addPoint(-0.1F, CubicSpline.builder($$1, $$5).addPoint(-0.05F, 2.67F).addPoint(0.05F, 6.3F).build())
         .addPoint(0.03F, $$6);
      if ($$4) {
         CubicSpline<C, I> $$8 = CubicSpline.builder($$1, $$5).addPoint(0.0F, $$3).addPoint(0.1F, 0.625F).build();
         CubicSpline<C, I> $$9 = CubicSpline.builder($$2, $$5).addPoint(-0.9F, $$3).addPoint(-0.69F, $$8).build();
         $$7.addPoint(0.35F, $$3).addPoint(0.45F, $$9).addPoint(0.55F, $$9).addPoint(0.62F, $$3);
      } else {
         CubicSpline<C, I> $$10 = CubicSpline.builder($$2, $$5).addPoint(-0.7F, $$6).addPoint(-0.15F, 1.37F).build();
         CubicSpline<C, I> $$11 = CubicSpline.builder($$2, $$5).addPoint(0.45F, $$6).addPoint(0.7F, 1.56F).build();
         $$7.addPoint(0.05F, $$11).addPoint(0.4F, $$11).addPoint(0.45F, $$10).addPoint(0.55F, $$10).addPoint(0.58F, $$3);
      }

      return $$7.build();
   }

   private static float calculateSlope(float $$0, float $$1, float $$2, float $$3) {
      return ($$1 - $$0) / ($$3 - $$2);
   }

   private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> buildMountainRidgeSplineWithPoints(
      I $$0, float $$1, boolean $$2, BoundedFloatFunction<Float> $$3
   ) {
      Builder<C, I> $$4 = CubicSpline.builder($$0, $$3);
      float $$5 = -0.7F;
      float $$6 = -1.0F;
      float $$7 = mountainContinentalness(-1.0F, $$1, -0.7F);
      float $$8 = 1.0F;
      float $$9 = mountainContinentalness(1.0F, $$1, -0.7F);
      float $$10 = calculateMountainRidgeZeroContinentalnessPoint($$1);
      float $$11 = -0.65F;
      if (-0.65F < $$10 && $$10 < 1.0F) {
         float $$12 = mountainContinentalness(-0.65F, $$1, -0.7F);
         float $$13 = -0.75F;
         float $$14 = mountainContinentalness(-0.75F, $$1, -0.7F);
         float $$15 = calculateSlope($$7, $$14, -1.0F, -0.75F);
         $$4.addPoint(-1.0F, $$7, $$15);
         $$4.addPoint(-0.75F, $$14);
         $$4.addPoint(-0.65F, $$12);
         float $$16 = mountainContinentalness($$10, $$1, -0.7F);
         float $$17 = calculateSlope($$16, $$9, $$10, 1.0F);
         float $$18 = 0.01F;
         $$4.addPoint($$10 - 0.01F, $$16);
         $$4.addPoint($$10, $$16, $$17);
         $$4.addPoint(1.0F, $$9, $$17);
      } else {
         float $$19 = calculateSlope($$7, $$9, -1.0F, 1.0F);
         if ($$2) {
            $$4.addPoint(-1.0F, Math.max(0.2F, $$7));
            $$4.addPoint(0.0F, Mth.lerp(0.5F, $$7, $$9), $$19);
         } else {
            $$4.addPoint(-1.0F, $$7, $$19);
         }

         $$4.addPoint(1.0F, $$9, $$19);
      }

      return $$4.build();
   }

   private static float mountainContinentalness(float $$0, float $$1, float $$2) {
      float $$3 = 1.17F;
      float $$4 = 0.46082947F;
      float $$5 = 1.0F - (1.0F - $$1) * 0.5F;
      float $$6 = 0.5F * (1.0F - $$1);
      float $$7 = ($$0 + 1.17F) * 0.46082947F;
      float $$8 = $$7 * $$5 - $$6;
      return $$0 < $$2 ? Math.max($$8, -0.2222F) : Math.max($$8, 0.0F);
   }

   private static float calculateMountainRidgeZeroContinentalnessPoint(float $$0) {
      float $$1 = 1.17F;
      float $$2 = 0.46082947F;
      float $$3 = 1.0F - (1.0F - $$0) * 0.5F;
      float $$4 = 0.5F * (1.0F - $$0);
      return $$4 / (0.46082947F * $$3) - 1.17F;
   }

   public static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> buildErosionOffsetSpline(
      I $$0, I $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7, boolean $$8, boolean $$9, BoundedFloatFunction<Float> $$10
   ) {
      float $$11 = 0.6F;
      float $$12 = 0.5F;
      float $$13 = 0.5F;
      CubicSpline<C, I> $$14 = buildMountainRidgeSplineWithPoints($$1, Mth.lerp($$5, 0.6F, 1.5F), $$9, $$10);
      CubicSpline<C, I> $$15 = buildMountainRidgeSplineWithPoints($$1, Mth.lerp($$5, 0.6F, 1.0F), $$9, $$10);
      CubicSpline<C, I> $$16 = buildMountainRidgeSplineWithPoints($$1, $$5, $$9, $$10);
      CubicSpline<C, I> $$17 = ridgeSpline($$1, $$2 - 0.15F, 0.5F * $$5, Mth.lerp(0.5F, 0.5F, 0.5F) * $$5, 0.5F * $$5, 0.6F * $$5, 0.5F, $$10);
      CubicSpline<C, I> $$18 = ridgeSpline($$1, $$2, $$6 * $$5, $$3 * $$5, 0.5F * $$5, 0.6F * $$5, 0.5F, $$10);
      CubicSpline<C, I> $$19 = ridgeSpline($$1, $$2, $$6, $$6, $$3, $$4, 0.5F, $$10);
      CubicSpline<C, I> $$20 = ridgeSpline($$1, $$2, $$6, $$6, $$3, $$4, 0.5F, $$10);
      CubicSpline<C, I> $$21 = CubicSpline.builder($$1, $$10).addPoint(-1.0F, $$2).addPoint(-0.4F, $$19).addPoint(0.0F, $$4 + 0.07F).build();
      CubicSpline<C, I> $$22 = ridgeSpline($$1, -0.02F, $$7, $$7, $$3, $$4, 0.0F, $$10);
      Builder<C, I> $$23 = CubicSpline.builder($$0, $$10)
         .addPoint(-0.85F, $$14)
         .addPoint(-0.7F, $$15)
         .addPoint(-0.4F, $$16)
         .addPoint(-0.35F, $$17)
         .addPoint(-0.1F, $$18)
         .addPoint(0.2F, $$19);
      if ($$8) {
         $$23.addPoint(0.4F, $$20).addPoint(0.45F, $$21).addPoint(0.55F, $$21).addPoint(0.58F, $$20);
      }

      $$23.addPoint(0.7F, $$22);
      return $$23.build();
   }

   private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> ridgeSpline(
      I $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, BoundedFloatFunction<Float> $$7
   ) {
      float $$8 = Math.max(0.5F * ($$2 - $$1), $$6);
      float $$9 = 5.0F * ($$3 - $$2);
      return CubicSpline.builder($$0, $$7)
         .addPoint(-1.0F, $$1, $$8)
         .addPoint(-0.4F, $$2, Math.min($$8, $$9))
         .addPoint(0.0F, $$3, $$9)
         .addPoint(0.4F, $$4, 2.0F * ($$4 - $$3))
         .addPoint(1.0F, $$5, 0.7F * ($$5 - $$4))
         .build();
   }
}
