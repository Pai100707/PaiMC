/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.debugchart;

import net.minecraft.util.debugchart.SampleLogger;

public abstract class AbstractSampleLogger
implements SampleLogger {
    protected final long[] defaults;
    protected final long[] sample;

    protected AbstractSampleLogger(int $$0, long[] $$1) {
        if ($$1.length != $$0) {
            throw new IllegalArgumentException("defaults have incorrect length of " + $$1.length);
        }
        this.sample = new long[$$0];
        this.defaults = $$1;
    }

    @Override
    public void logFullSample(long[] $$0) {
        System.arraycopy($$0, 0, this.sample, 0, $$0.length);
        this.useSample();
        this.resetSample();
    }

    @Override
    public void logSample(long $$0) {
        this.sample[0] = $$0;
        this.useSample();
        this.resetSample();
    }

    @Override
    public void logPartialSample(long $$0, int $$1) {
        if ($$1 < 1 || $$1 >= this.sample.length) {
            throw new IndexOutOfBoundsException($$1 + " out of bounds for dimensions " + this.sample.length);
        }
        this.sample[$$1] = $$0;
    }

    protected abstract void useSample();

    protected void resetSample() {
        System.arraycopy(this.defaults, 0, this.sample, 0, this.defaults.length);
    }
}

