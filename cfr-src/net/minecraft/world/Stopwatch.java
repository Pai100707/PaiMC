/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

public record Stopwatch(long creationTime, long accumulatedElapsedTime) {
    public Stopwatch(long $$0) {
        this($$0, 0L);
    }

    public long elapsedMilliseconds(long $$0) {
        long $$1 = $$0 - this.creationTime;
        return this.accumulatedElapsedTime + $$1;
    }

    public double elapsedSeconds(long $$0) {
        return (double)this.elapsedMilliseconds($$0) / 1000.0;
    }
}

