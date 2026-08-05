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
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

public class UniformFloat
extends FloatProvider {
    public static final MapCodec<UniformFloat> CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)Codec.FLOAT.fieldOf("min_inclusive").forGetter($$0 -> Float.valueOf($$0.minInclusive)), (App)Codec.FLOAT.fieldOf("max_exclusive").forGetter($$0 -> Float.valueOf($$0.maxExclusive))).apply((Applicative)$$02, UniformFloat::new)).validate($$0 -> {
        if ($$0.maxExclusive <= $$0.minInclusive) {
            return DataResult.error(() -> "Max must be larger than min, min_inclusive: " + $$0.minInclusive + ", max_exclusive: " + $$0.maxExclusive);
        }
        return DataResult.success((Object)$$0);
    });
    private final float minInclusive;
    private final float maxExclusive;

    private UniformFloat(float $$0, float $$1) {
        this.minInclusive = $$0;
        this.maxExclusive = $$1;
    }

    public static UniformFloat of(float $$0, float $$1) {
        if ($$1 <= $$0) {
            throw new IllegalArgumentException("Max must exceed min");
        }
        return new UniformFloat($$0, $$1);
    }

    @Override
    public float sample(RandomSource $$0) {
        return Mth.randomBetween($$0, this.minInclusive, this.maxExclusive);
    }

    @Override
    public float getMinValue() {
        return this.minInclusive;
    }

    @Override
    public float getMaxValue() {
        return this.maxExclusive;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxExclusive + "]";
    }
}

