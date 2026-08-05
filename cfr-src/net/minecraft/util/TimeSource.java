/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

@FunctionalInterface
public interface TimeSource {
    public long get(TimeUnit var1);

    public static interface NanoTimeSource
    extends TimeSource,
    LongSupplier {
        @Override
        default public long get(TimeUnit $$0) {
            return $$0.convert(this.getAsLong(), TimeUnit.NANOSECONDS);
        }
    }
}

