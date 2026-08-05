/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.attribute;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class GaussianSampler {
    private static final int GAUSSIAN_SAMPLE_RADIUS = 2;
    private static final int GAUSSIAN_SAMPLE_BREADTH = 6;
    private static final double[] GAUSSIAN_SAMPLE_KERNEL = new double[]{0.0, 1.0, 4.0, 6.0, 4.0, 1.0, 0.0};

    public static <V> void sample(Vec3 $$0, Sampler<V> $$1, Accumulator<V> $$2) {
        $$0 = $$0.subtract(0.5, 0.5, 0.5);
        int $$3 = Mth.floor($$0.x());
        int $$4 = Mth.floor($$0.y());
        int $$5 = Mth.floor($$0.z());
        double $$6 = $$0.x() - (double)$$3;
        double $$7 = $$0.y() - (double)$$4;
        double $$8 = $$0.z() - (double)$$5;
        for (int $$9 = 0; $$9 < 6; ++$$9) {
            double $$10 = Mth.lerp($$8, GAUSSIAN_SAMPLE_KERNEL[$$9 + 1], GAUSSIAN_SAMPLE_KERNEL[$$9]);
            int $$11 = $$5 - 2 + $$9;
            for (int $$12 = 0; $$12 < 6; ++$$12) {
                double $$13 = Mth.lerp($$6, GAUSSIAN_SAMPLE_KERNEL[$$12 + 1], GAUSSIAN_SAMPLE_KERNEL[$$12]);
                int $$14 = $$3 - 2 + $$12;
                for (int $$15 = 0; $$15 < 6; ++$$15) {
                    double $$16 = Mth.lerp($$7, GAUSSIAN_SAMPLE_KERNEL[$$15 + 1], GAUSSIAN_SAMPLE_KERNEL[$$15]);
                    int $$17 = $$4 - 2 + $$15;
                    double $$18 = $$13 * $$16 * $$10;
                    V $$19 = $$1.get($$14, $$17, $$11);
                    $$2.accumulate($$18, $$19);
                }
            }
        }
    }

    @FunctionalInterface
    public static interface Sampler<V> {
        public V get(int var1, int var2, int var3);
    }

    @FunctionalInterface
    public static interface Accumulator<V> {
        public void accumulate(double var1, V var3);
    }
}

