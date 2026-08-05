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

public class ClampedNormalInt
extends IntProvider {
    public static final MapCodec<ClampedNormalInt> CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)Codec.FLOAT.fieldOf("mean").forGetter($$0 -> Float.valueOf($$0.mean)), (App)Codec.FLOAT.fieldOf("deviation").forGetter($$0 -> Float.valueOf($$0.deviation)), (App)Codec.INT.fieldOf("min_inclusive").forGetter($$0 -> $$0.minInclusive), (App)Codec.INT.fieldOf("max_inclusive").forGetter($$0 -> $$0.maxInclusive)).apply((Applicative)$$02, ClampedNormalInt::new)).validate($$0 -> {
        if ($$0.maxInclusive < $$0.minInclusive) {
            return DataResult.error(() -> "Max must be larger than min: [" + $$0.minInclusive + ", " + $$0.maxInclusive + "]");
        }
        return DataResult.success((Object)$$0);
    });
    private final float mean;
    private final float deviation;
    private final int minInclusive;
    private final int maxInclusive;

    public static ClampedNormalInt of(float $$0, float $$1, int $$2, int $$3) {
        return new ClampedNormalInt($$0, $$1, $$2, $$3);
    }

    private ClampedNormalInt(float $$0, float $$1, int $$2, int $$3) {
        this.mean = $$0;
        this.deviation = $$1;
        this.minInclusive = $$2;
        this.maxInclusive = $$3;
    }

    @Override
    public int sample(RandomSource $$0) {
        return ClampedNormalInt.sample($$0, this.mean, this.deviation, this.minInclusive, this.maxInclusive);
    }

    public static int sample(RandomSource $$0, float $$1, float $$2, float $$3, float $$4) {
        return (int)Mth.clamp(Mth.normal($$0, $$1, $$2), $$3, $$4);
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
        return IntProviderType.CLAMPED_NORMAL;
    }

    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}

