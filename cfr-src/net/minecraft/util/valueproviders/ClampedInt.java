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

public class ClampedInt
extends IntProvider {
    public static final MapCodec<ClampedInt> CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)IntProvider.CODEC.fieldOf("source").forGetter($$0 -> $$0.source), (App)Codec.INT.fieldOf("min_inclusive").forGetter($$0 -> $$0.minInclusive), (App)Codec.INT.fieldOf("max_inclusive").forGetter($$0 -> $$0.maxInclusive)).apply((Applicative)$$02, ClampedInt::new)).validate($$0 -> {
        if ($$0.maxInclusive < $$0.minInclusive) {
            return DataResult.error(() -> "Max must be at least min, min_inclusive: " + $$0.minInclusive + ", max_inclusive: " + $$0.maxInclusive);
        }
        return DataResult.success((Object)$$0);
    });
    private final IntProvider source;
    private final int minInclusive;
    private final int maxInclusive;

    public static ClampedInt of(IntProvider $$0, int $$1, int $$2) {
        return new ClampedInt($$0, $$1, $$2);
    }

    public ClampedInt(IntProvider $$0, int $$1, int $$2) {
        this.source = $$0;
        this.minInclusive = $$1;
        this.maxInclusive = $$2;
    }

    @Override
    public int sample(RandomSource $$0) {
        return Mth.clamp(this.source.sample($$0), this.minInclusive, this.maxInclusive);
    }

    @Override
    public int getMinValue() {
        return Math.max(this.minInclusive, this.source.getMinValue());
    }

    @Override
    public int getMaxValue() {
        return Math.min(this.maxInclusive, this.source.getMaxValue());
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.CLAMPED;
    }
}

