/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen;

import net.minecraft.util.BoundedFloatFunction;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class TerrainProvider {
    private static final float DEEP_OCEAN_CONTINENTALNESS = -0.51f;
    private static final float OCEAN_CONTINENTALNESS = -0.4f;
    private static final float PLAINS_CONTINENTALNESS = 0.1f;
    private static final float BEACH_CONTINENTALNESS = -0.15f;
    private static final BoundedFloatFunction<Float> NO_TRANSFORM = BoundedFloatFunction.IDENTITY;
    private static final BoundedFloatFunction<Float> AMPLIFIED_OFFSET = BoundedFloatFunction.createUnlimited($$0 -> $$0 < 0.0f ? $$0 : $$0 * 2.0f);
    private static final BoundedFloatFunction<Float> AMPLIFIED_FACTOR = BoundedFloatFunction.createUnlimited($$0 -> 1.25f - 6.25f / ($$0 + 5.0f));
    private static final BoundedFloatFunction<Float> AMPLIFIED_JAGGEDNESS = BoundedFloatFunction.createUnlimited($$0 -> $$0 * 2.0f);

    public static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> overworldOffset(I $$0, I $$1, I $$2, boolean $$3) {
        BoundedFloatFunction<Float> $$4 = $$3 ? AMPLIFIED_OFFSET : NO_TRANSFORM;
        CubicSpline<C, I> $$5 = TerrainProvider.buildErosionOffsetSpline($$1, $$2, -0.15f, 0.0f, 0.0f, 0.1f, 0.0f, -0.03f, false, false, $$4);
        CubicSpline<C, I> $$6 = TerrainProvider.buildErosionOffsetSpline($$1, $$2, -0.1f, 0.03f, 0.1f, 0.1f, 0.01f, -0.03f, false, false, $$4);
        CubicSpline<C, I> $$7 = TerrainProvider.buildErosionOffsetSpline($$1, $$2, -0.1f, 0.03f, 0.1f, 0.7f, 0.01f, -0.03f, true, true, $$4);
        CubicSpline<C, I> $$8 = TerrainProvider.buildErosionOffsetSpline($$1, $$2, -0.05f, 0.03f, 0.1f, 1.0f, 0.01f, 0.01f, true, true, $$4);
        return CubicSpline.builder($$0, $$4).addPoint(-1.1f, 0.044f).addPoint(-1.02f, -0.2222f).addPoint(-0.51f, -0.2222f).addPoint(-0.44f, -0.12f).addPoint(-0.18f, -0.12f).addPoint(-0.16f, $$5).addPoint(-0.15f, $$5).addPoint(-0.1f, $$6).addPoint(0.25f, $$7).addPoint(1.0f, $$8).build();
    }

    public static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> overworldFactor(I $$0, I $$1, I $$2, I $$3, boolean $$4) {
        BoundedFloatFunction<Float> $$5 = $$4 ? AMPLIFIED_FACTOR : NO_TRANSFORM;
        return CubicSpline.builder($$0, NO_TRANSFORM).addPoint(-0.19f, 3.95f).addPoint(-0.15f, TerrainProvider.getErosionFactor($$1, $$2, $$3, 6.25f, true, NO_TRANSFORM)).addPoint(-0.1f, TerrainProvider.getErosionFactor($$1, $$2, $$3, 5.47f, true, $$5)).addPoint(0.03f, TerrainProvider.getErosionFactor($$1, $$2, $$3, 5.08f, true, $$5)).addPoint(0.06f, TerrainProvider.getErosionFactor($$1, $$2, $$3, 4.69f, false, $$5)).build();
    }

    public static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> overworldJaggedness(I $$0, I $$1, I $$2, I $$3, boolean $$4) {
        BoundedFloatFunction<Float> $$5 = $$4 ? AMPLIFIED_JAGGEDNESS : NO_TRANSFORM;
        float $$6 = 0.65f;
        return CubicSpline.builder($$0, $$5).addPoint(-0.11f, 0.0f).addPoint(0.03f, TerrainProvider.buildErosionJaggednessSpline($$1, $$2, $$3, 1.0f, 0.5f, 0.0f, 0.0f, $$5)).addPoint(0.65f, TerrainProvider.buildErosionJaggednessSpline($$1, $$2, $$3, 1.0f, 1.0f, 1.0f, 0.0f, $$5)).build();
    }

    private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> buildErosionJaggednessSpline(I $$0, I $$1, I $$2, float $$3, float $$4, float $$5, float $$6, BoundedFloatFunction<Float> $$7) {
        float $$8 = -0.5775f;
        CubicSpline<C, I> $$9 = TerrainProvider.buildRidgeJaggednessSpline($$1, $$2, $$3, $$5, $$7);
        CubicSpline<C, I> $$10 = TerrainProvider.buildRidgeJaggednessSpline($$1, $$2, $$4, $$6, $$7);
        return CubicSpline.builder($$0, $$7).addPoint(-1.0f, $$9).addPoint(-0.78f, $$10).addPoint(-0.5775f, $$10).addPoint(-0.375f, 0.0f).build();
    }

    private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> buildRidgeJaggednessSpline(I $$0, I $$1, float $$2, float $$3, BoundedFloatFunction<Float> $$4) {
        float $$5 = NoiseRouterData.peaksAndValleys(0.4f);
        float $$6 = NoiseRouterData.peaksAndValleys(0.56666666f);
        float $$7 = ($$5 + $$6) / 2.0f;
        CubicSpline.Builder<C, I> $$8 = CubicSpline.builder($$1, $$4);
        $$8.addPoint($$5, 0.0f);
        if ($$3 > 0.0f) {
            $$8.addPoint($$7, TerrainProvider.buildWeirdnessJaggednessSpline($$0, $$3, $$4));
        } else {
            $$8.addPoint($$7, 0.0f);
        }
        if ($$2 > 0.0f) {
            $$8.addPoint(1.0f, TerrainProvider.buildWeirdnessJaggednessSpline($$0, $$2, $$4));
        } else {
            $$8.addPoint(1.0f, 0.0f);
        }
        return $$8.build();
    }

    private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> buildWeirdnessJaggednessSpline(I $$0, float $$1, BoundedFloatFunction<Float> $$2) {
        float $$3 = 0.63f * $$1;
        float $$4 = 0.3f * $$1;
        return CubicSpline.builder($$0, $$2).addPoint(-0.01f, $$3).addPoint(0.01f, $$4).build();
    }

    private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> getErosionFactor(I $$0, I $$1, I $$2, float $$3, boolean $$4, BoundedFloatFunction<Float> $$5) {
        CubicSpline $$6 = CubicSpline.builder($$1, $$5).addPoint(-0.2f, 6.3f).addPoint(0.2f, $$3).build();
        CubicSpline.Builder $$7 = CubicSpline.builder($$0, $$5).addPoint(-0.6f, $$6).addPoint(-0.5f, CubicSpline.builder($$1, $$5).addPoint(-0.05f, 6.3f).addPoint(0.05f, 2.67f).build()).addPoint(-0.35f, $$6).addPoint(-0.25f, $$6).addPoint(-0.1f, CubicSpline.builder($$1, $$5).addPoint(-0.05f, 2.67f).addPoint(0.05f, 6.3f).build()).addPoint(0.03f, $$6);
        if ($$4) {
            CubicSpline $$8 = CubicSpline.builder($$1, $$5).addPoint(0.0f, $$3).addPoint(0.1f, 0.625f).build();
            CubicSpline $$9 = CubicSpline.builder($$2, $$5).addPoint(-0.9f, $$3).addPoint(-0.69f, $$8).build();
            $$7.addPoint(0.35f, $$3).addPoint(0.45f, $$9).addPoint(0.55f, $$9).addPoint(0.62f, $$3);
        } else {
            CubicSpline $$10 = CubicSpline.builder($$2, $$5).addPoint(-0.7f, $$6).addPoint(-0.15f, 1.37f).build();
            CubicSpline $$11 = CubicSpline.builder($$2, $$5).addPoint(0.45f, $$6).addPoint(0.7f, 1.56f).build();
            $$7.addPoint(0.05f, $$11).addPoint(0.4f, $$11).addPoint(0.45f, $$10).addPoint(0.55f, $$10).addPoint(0.58f, $$3);
        }
        return $$7.build();
    }

    private static float calculateSlope(float $$0, float $$1, float $$2, float $$3) {
        return ($$1 - $$0) / ($$3 - $$2);
    }

    private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> buildMountainRidgeSplineWithPoints(I $$0, float $$1, boolean $$2, BoundedFloatFunction<Float> $$3) {
        CubicSpline.Builder $$4 = CubicSpline.builder($$0, $$3);
        float $$5 = -0.7f;
        float $$6 = -1.0f;
        float $$7 = TerrainProvider.mountainContinentalness(-1.0f, $$1, -0.7f);
        float $$8 = 1.0f;
        float $$9 = TerrainProvider.mountainContinentalness(1.0f, $$1, -0.7f);
        float $$10 = TerrainProvider.calculateMountainRidgeZeroContinentalnessPoint($$1);
        float $$11 = -0.65f;
        if (-0.65f < $$10 && $$10 < 1.0f) {
            float $$12 = TerrainProvider.mountainContinentalness(-0.65f, $$1, -0.7f);
            float $$13 = -0.75f;
            float $$14 = TerrainProvider.mountainContinentalness(-0.75f, $$1, -0.7f);
            float $$15 = TerrainProvider.calculateSlope($$7, $$14, -1.0f, -0.75f);
            $$4.addPoint(-1.0f, $$7, $$15);
            $$4.addPoint(-0.75f, $$14);
            $$4.addPoint(-0.65f, $$12);
            float $$16 = TerrainProvider.mountainContinentalness($$10, $$1, -0.7f);
            float $$17 = TerrainProvider.calculateSlope($$16, $$9, $$10, 1.0f);
            float $$18 = 0.01f;
            $$4.addPoint($$10 - 0.01f, $$16);
            $$4.addPoint($$10, $$16, $$17);
            $$4.addPoint(1.0f, $$9, $$17);
        } else {
            float $$19 = TerrainProvider.calculateSlope($$7, $$9, -1.0f, 1.0f);
            if ($$2) {
                $$4.addPoint(-1.0f, Math.max(0.2f, $$7));
                $$4.addPoint(0.0f, Mth.lerp(0.5f, $$7, $$9), $$19);
            } else {
                $$4.addPoint(-1.0f, $$7, $$19);
            }
            $$4.addPoint(1.0f, $$9, $$19);
        }
        return $$4.build();
    }

    private static float mountainContinentalness(float $$0, float $$1, float $$2) {
        float $$3 = 1.17f;
        float $$4 = 0.46082947f;
        float $$5 = 1.0f - (1.0f - $$1) * 0.5f;
        float $$6 = 0.5f * (1.0f - $$1);
        float $$7 = ($$0 + 1.17f) * 0.46082947f;
        float $$8 = $$7 * $$5 - $$6;
        if ($$0 < $$2) {
            return Math.max($$8, -0.2222f);
        }
        return Math.max($$8, 0.0f);
    }

    private static float calculateMountainRidgeZeroContinentalnessPoint(float $$0) {
        float $$1 = 1.17f;
        float $$2 = 0.46082947f;
        float $$3 = 1.0f - (1.0f - $$0) * 0.5f;
        float $$4 = 0.5f * (1.0f - $$0);
        return $$4 / (0.46082947f * $$3) - 1.17f;
    }

    public static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> buildErosionOffsetSpline(I $$0, I $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7, boolean $$8, boolean $$9, BoundedFloatFunction<Float> $$10) {
        float $$11 = 0.6f;
        float $$12 = 0.5f;
        float $$13 = 0.5f;
        CubicSpline<C, I> $$14 = TerrainProvider.buildMountainRidgeSplineWithPoints($$1, Mth.lerp($$5, 0.6f, 1.5f), $$9, $$10);
        CubicSpline<C, I> $$15 = TerrainProvider.buildMountainRidgeSplineWithPoints($$1, Mth.lerp($$5, 0.6f, 1.0f), $$9, $$10);
        CubicSpline<C, I> $$16 = TerrainProvider.buildMountainRidgeSplineWithPoints($$1, $$5, $$9, $$10);
        CubicSpline<C, I> $$17 = TerrainProvider.ridgeSpline($$1, $$2 - 0.15f, 0.5f * $$5, Mth.lerp(0.5f, 0.5f, 0.5f) * $$5, 0.5f * $$5, 0.6f * $$5, 0.5f, $$10);
        CubicSpline<C, I> $$18 = TerrainProvider.ridgeSpline($$1, $$2, $$6 * $$5, $$3 * $$5, 0.5f * $$5, 0.6f * $$5, 0.5f, $$10);
        CubicSpline<C, I> $$19 = TerrainProvider.ridgeSpline($$1, $$2, $$6, $$6, $$3, $$4, 0.5f, $$10);
        CubicSpline<C, I> $$20 = TerrainProvider.ridgeSpline($$1, $$2, $$6, $$6, $$3, $$4, 0.5f, $$10);
        CubicSpline $$21 = CubicSpline.builder($$1, $$10).addPoint(-1.0f, $$2).addPoint(-0.4f, $$19).addPoint(0.0f, $$4 + 0.07f).build();
        CubicSpline<C, I> $$22 = TerrainProvider.ridgeSpline($$1, -0.02f, $$7, $$7, $$3, $$4, 0.0f, $$10);
        CubicSpline.Builder<C, I> $$23 = CubicSpline.builder($$0, $$10).addPoint(-0.85f, $$14).addPoint(-0.7f, $$15).addPoint(-0.4f, $$16).addPoint(-0.35f, $$17).addPoint(-0.1f, $$18).addPoint(0.2f, $$19);
        if ($$8) {
            $$23.addPoint(0.4f, $$20).addPoint(0.45f, $$21).addPoint(0.55f, $$21).addPoint(0.58f, $$20);
        }
        $$23.addPoint(0.7f, $$22);
        return $$23.build();
    }

    private static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> ridgeSpline(I $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, BoundedFloatFunction<Float> $$7) {
        float $$8 = Math.max(0.5f * ($$2 - $$1), $$6);
        float $$9 = 5.0f * ($$3 - $$2);
        return CubicSpline.builder($$0, $$7).addPoint(-1.0f, $$1, $$8).addPoint(-0.4f, $$2, Math.min($$8, $$9)).addPoint(0.0f, $$3, $$9).addPoint(0.4f, $$4, 2.0f * ($$4 - $$3)).addPoint(1.0f, $$5, 0.7f * ($$5 - $$4)).build();
    }
}

