/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.ThreadLocalRandom
 */
package net.minecraft.util;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;

public interface RandomSource {
    @Deprecated
    public static final double GAUSSIAN_SPREAD_FACTOR = 2.297;

    public static RandomSource create() {
        return RandomSource.create(RandomSupport.generateUniqueSeed());
    }

    @Deprecated
    public static RandomSource createThreadSafe() {
        return new ThreadSafeLegacyRandomSource(RandomSupport.generateUniqueSeed());
    }

    public static RandomSource create(long $$0) {
        return new LegacyRandomSource($$0);
    }

    public static RandomSource createNewThreadLocalInstance() {
        return new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong());
    }

    public RandomSource fork();

    public PositionalRandomFactory forkPositional();

    public void setSeed(long var1);

    public int nextInt();

    public int nextInt(int var1);

    default public int nextIntBetweenInclusive(int $$0, int $$1) {
        return this.nextInt($$1 - $$0 + 1) + $$0;
    }

    public long nextLong();

    public boolean nextBoolean();

    public float nextFloat();

    public double nextDouble();

    public double nextGaussian();

    default public double triangle(double $$0, double $$1) {
        return $$0 + $$1 * (this.nextDouble() - this.nextDouble());
    }

    default public float triangle(float $$0, float $$1) {
        return $$0 + $$1 * (this.nextFloat() - this.nextFloat());
    }

    default public void consumeCount(int $$0) {
        for (int $$1 = 0; $$1 < $$0; ++$$1) {
            this.nextInt();
        }
    }

    default public int nextInt(int $$0, int $$1) {
        if ($$0 >= $$1) {
            throw new IllegalArgumentException("bound - origin is non positive");
        }
        return $$0 + this.nextInt($$1 - $$0);
    }
}

