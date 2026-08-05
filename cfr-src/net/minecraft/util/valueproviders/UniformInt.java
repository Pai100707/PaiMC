/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class UniformInt
extends IntProvider {
    public static final MapCodec<UniformInt> CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)Codec.INT.fieldOf("min_inclusive").forGetter($$0 -> $$0.minInclusive), (App)Codec.INT.fieldOf("max_inclusive").forGetter($$0 -> $$0.maxInclusive)).apply((Applicative)$$02, UniformInt::new)).validate($$0 -> {
        if ($$0.maxInclusive < $$0.minInclusive) {
            return DataResult.error(() -> "Max must be at least min, min_inclusive: " + $$0.minInclusive + ", max_inclusive: " + $$0.maxInclusive);
        }
        return DataResult.success((Object)$$0);
    });
    private final int minInclusive;
    private final int maxInclusive;

    private UniformInt(int $$0, int $$1) {
        this.minInclusive = $$0;
        this.maxInclusive = $$1;
    }

    public static UniformInt of(int $$0, int $$1) {
        return new UniformInt($$0, $$1);
    }

    @Override
    public int sample(RandomSource $$0) {
        return Mth.randomBetweenInclusive($$0, this.minInclusive, this.maxInclusive);
    }

    @Override
    public int getMinValue() {
        return this.minInclusive;
    }

    @Override
    public int getMaxValue() {
        return this.maxInclusive;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}

