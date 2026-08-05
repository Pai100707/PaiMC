/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public class Optionull {
    @Deprecated
    public static <T> T orElse(@Nullable T $$0, T $$1) {
        return Objects.requireNonNullElse($$0, $$1);
    }

    public static <T, R> @Nullable R map(@Nullable T $$0, Function<T, R> $$1) {
        return $$0 == null ? null : (R)$$1.apply($$0);
    }

    public static <T, R> R mapOrDefault(@Nullable T $$0, Function<T, R> $$1, R $$2) {
        return $$0 == null ? $$2 : $$1.apply($$0);
    }

    public static <T, R> R mapOrElse(@Nullable T $$0, Function<T, R> $$1, Supplier<R> $$2) {
        return $$0 == null ? $$2.get() : $$1.apply($$0);
    }

    public static <T> @Nullable T first(Collection<T> $$0) {
        Iterator<T> $$1 = $$0.iterator();
        return $$1.hasNext() ? (T)$$1.next() : null;
    }

    public static <T> T firstOrDefault(Collection<T> $$0, T $$1) {
        Iterator<T> $$2 = $$0.iterator();
        return $$2.hasNext() ? $$2.next() : $$1;
    }

    public static <T> T firstOrElse(Collection<T> $$0, Supplier<T> $$1) {
        Iterator<T> $$2 = $$0.iterator();
        return $$2.hasNext() ? $$2.next() : $$1.get();
    }

    public static <T> boolean isNullOrEmpty(T @Nullable [] $$0) {
        return $$0 == null || $$0.length == 0;
    }

    public static boolean isNullOrEmpty(boolean @Nullable [] $$0) {
        return $$0 == null || $$0.length == 0;
    }

    public static boolean isNullOrEmpty(byte @Nullable [] $$0) {
        return $$0 == null || $$0.length == 0;
    }

    public static boolean isNullOrEmpty(char @Nullable [] $$0) {
        return $$0 == null || $$0.length == 0;
    }

    public static boolean isNullOrEmpty(short @Nullable [] $$0) {
        return $$0 == null || $$0.length == 0;
    }

    public static boolean isNullOrEmpty(int @Nullable [] $$0) {
        return $$0 == null || $$0.length == 0;
    }

    public static boolean isNullOrEmpty(long @Nullable [] $$0) {
        return $$0 == null || $$0.length == 0;
    }

    public static boolean isNullOrEmpty(float @Nullable [] $$0) {
        return $$0 == null || $$0.length == 0;
    }

    public static boolean isNullOrEmpty(double @Nullable [] $$0) {
        return $$0 == null || $$0.length == 0;
    }
}

