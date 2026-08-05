/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;

public record InclusiveRange<T extends Comparable<T>>(T minInclusive, T maxInclusive) {
    public static final Codec<InclusiveRange<Integer>> INT = InclusiveRange.codec(Codec.INT);

    public InclusiveRange {
        if ($$0.compareTo($$1) > 0) {
            throw new IllegalArgumentException("min_inclusive must be less than or equal to max_inclusive");
        }
    }

    public InclusiveRange(T $$0) {
        this($$0, $$0);
    }

    public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> $$0) {
        return ExtraCodecs.intervalCodec($$0, "min_inclusive", "max_inclusive", InclusiveRange::create, InclusiveRange::minInclusive, InclusiveRange::maxInclusive);
    }

    public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> $$0, T $$1, T $$22) {
        return InclusiveRange.codec($$0).validate($$2 -> {
            if ($$2.minInclusive().compareTo($$1) < 0) {
                return DataResult.error(() -> "Range limit too low, expected at least " + String.valueOf($$1) + " [" + String.valueOf($$2.minInclusive()) + "-" + String.valueOf($$2.maxInclusive()) + "]");
            }
            if ($$2.maxInclusive().compareTo($$22) > 0) {
                return DataResult.error(() -> "Range limit too high, expected at most " + String.valueOf($$22) + " [" + String.valueOf($$2.minInclusive()) + "-" + String.valueOf($$2.maxInclusive()) + "]");
            }
            return DataResult.success((Object)$$2);
        });
    }

    public static <T extends Comparable<T>> DataResult<InclusiveRange<T>> create(T $$0, T $$1) {
        if ($$0.compareTo($$1) <= 0) {
            return DataResult.success(new InclusiveRange<T>($$0, $$1));
        }
        return DataResult.error(() -> "min_inclusive must be less than or equal to max_inclusive");
    }

    public <S extends Comparable<S>> InclusiveRange<S> map(Function<? super T, ? extends S> $$0) {
        return new InclusiveRange<Comparable>((Comparable)$$0.apply(this.minInclusive), (Comparable)$$0.apply(this.maxInclusive));
    }

    public boolean isValueInRange(T $$0) {
        return $$0.compareTo(this.minInclusive) >= 0 && $$0.compareTo(this.maxInclusive) <= 0;
    }

    public boolean contains(InclusiveRange<T> $$0) {
        return $$0.minInclusive().compareTo(this.minInclusive) >= 0 && $$0.maxInclusive.compareTo(this.maxInclusive) <= 0;
    }

    @Override
    public String toString() {
        return "[" + String.valueOf(this.minInclusive) + ", " + String.valueOf(this.maxInclusive) + "]";
    }
}

