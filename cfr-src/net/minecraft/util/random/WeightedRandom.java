/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.random;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

public class WeightedRandom {
    private WeightedRandom() {
    }

    public static <T> int getTotalWeight(List<T> $$0, ToIntFunction<T> $$1) {
        long $$2 = 0L;
        for (T $$3 : $$0) {
            $$2 += (long)$$1.applyAsInt($$3);
        }
        if ($$2 > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        return (int)$$2;
    }

    public static <T> Optional<T> getRandomItem(RandomSource $$0, List<T> $$1, int $$2, ToIntFunction<T> $$3) {
        if ($$2 < 0) {
            throw Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
        }
        if ($$2 == 0) {
            return Optional.empty();
        }
        int $$4 = $$0.nextInt($$2);
        return WeightedRandom.getWeightedItem($$1, $$4, $$3);
    }

    public static <T> Optional<T> getWeightedItem(List<T> $$0, int $$1, ToIntFunction<T> $$2) {
        for (T $$3 : $$0) {
            if (($$1 -= $$2.applyAsInt($$3)) >= 0) continue;
            return Optional.of($$3);
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getRandomItem(RandomSource $$0, List<T> $$1, ToIntFunction<T> $$2) {
        return WeightedRandom.getRandomItem($$0, $$1, WeightedRandom.getTotalWeight($$1, $$2), $$2);
    }
}

