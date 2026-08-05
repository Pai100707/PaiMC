/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class WeightedListInt
extends IntProvider {
    public static final MapCodec<WeightedListInt> CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)WeightedList.nonEmptyCodec(IntProvider.CODEC).fieldOf("distribution").forGetter($$0 -> $$0.distribution)).apply((Applicative)$$02, WeightedListInt::new));
    private final WeightedList<IntProvider> distribution;
    private final int minValue;
    private final int maxValue;

    public WeightedListInt(WeightedList<IntProvider> $$0) {
        this.distribution = $$0;
        int $$1 = Integer.MAX_VALUE;
        int $$2 = Integer.MIN_VALUE;
        for (Weighted<IntProvider> $$3 : $$0.unwrap()) {
            int $$4 = $$3.value().getMinValue();
            int $$5 = $$3.value().getMaxValue();
            $$1 = Math.min($$1, $$4);
            $$2 = Math.max($$2, $$5);
        }
        this.minValue = $$1;
        this.maxValue = $$2;
    }

    @Override
    public int sample(RandomSource $$0) {
        return this.distribution.getRandomOrThrow($$0).sample($$0);
    }

    @Override
    public int getMinValue() {
        return this.minValue;
    }

    @Override
    public int getMaxValue() {
        return this.maxValue;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.WEIGHTED_LIST;
    }
}

