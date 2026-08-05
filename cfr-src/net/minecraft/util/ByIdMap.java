/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.util.Mth;

public class ByIdMap {
    private static <T> IntFunction<T> createMap(ToIntFunction<T> $$0, T[] $$1) {
        if ($$1.length == 0) {
            throw new IllegalArgumentException("Empty value list");
        }
        Int2ObjectOpenHashMap $$2 = new Int2ObjectOpenHashMap();
        for (T $$3 : $$1) {
            int $$4 = $$0.applyAsInt($$3);
            Object $$5 = $$2.put($$4, $$3);
            if ($$5 == null) continue;
            throw new IllegalArgumentException("Duplicate entry on id " + $$4 + ": current=" + String.valueOf($$3) + ", previous=" + String.valueOf($$5));
        }
        return $$2;
    }

    public static <T> IntFunction<T> sparse(ToIntFunction<T> $$0, T[] $$1, T $$22) {
        IntFunction $$3 = ByIdMap.createMap($$0, $$1);
        return $$2 -> Objects.requireNonNullElse($$3.apply($$2), $$22);
    }

    private static <T> T[] createSortedArray(ToIntFunction<T> $$0, T[] $$1) {
        int $$2 = $$1.length;
        if ($$2 == 0) {
            throw new IllegalArgumentException("Empty value list");
        }
        Object[] $$3 = (Object[])$$1.clone();
        Arrays.fill($$3, null);
        for (T $$4 : $$1) {
            int $$5 = $$0.applyAsInt($$4);
            if ($$5 < 0 || $$5 >= $$2) {
                throw new IllegalArgumentException("Values are not continous, found index " + $$5 + " for value " + String.valueOf($$4));
            }
            Object $$6 = $$3[$$5];
            if ($$6 != null) {
                throw new IllegalArgumentException("Duplicate entry on id " + $$5 + ": current=" + String.valueOf($$4) + ", previous=" + String.valueOf($$6));
            }
            $$3[$$5] = $$4;
        }
        for (int $$7 = 0; $$7 < $$2; ++$$7) {
            if ($$3[$$7] != null) continue;
            throw new IllegalArgumentException("Missing value at index: " + $$7);
        }
        return $$3;
    }

    public static <T> IntFunction<T> continuous(ToIntFunction<T> $$0, T[] $$1, OutOfBoundsStrategy $$22) {
        Object[] $$32 = ByIdMap.createSortedArray($$0, $$1);
        int $$4 = $$32.length;
        return switch ($$22.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Object $$5 = $$32[0];
                yield $$3 -> $$3 >= 0 && $$3 < $$4 ? $$32[$$3] : $$5;
            }
            case 1 -> $$2 -> $$32[Mth.positiveModulo($$2, $$4)];
            case 2 -> $$2 -> $$32[Mth.clamp($$2, 0, $$4 - 1)];
        };
    }

    public static enum OutOfBoundsStrategy {
        ZERO,
        WRAP,
        CLAMP;

    }
}

