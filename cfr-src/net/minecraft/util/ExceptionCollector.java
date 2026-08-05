/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import org.jspecify.annotations.Nullable;

public class ExceptionCollector<T extends Throwable> {
    private @Nullable T result;

    public void add(T $$0) {
        if (this.result == null) {
            this.result = $$0;
        } else {
            ((Throwable)this.result).addSuppressed((Throwable)$$0);
        }
    }

    public void throwIfPresent() throws T {
        if (this.result != null) {
            throw this.result;
        }
    }
}

