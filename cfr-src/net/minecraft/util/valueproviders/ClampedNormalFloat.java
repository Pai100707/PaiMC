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

public class ClampedNormalFloat
extends FloatProvider {
    public static final MapCodec<ClampedNormalFloat> CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)Codec.FLOAT.fieldOf("mean").forGetter($$0 -> Float.valueOf($$0.mean)), (App)Codec.FLOAT.fieldOf("deviation").forGetter($$0 -> Float.valueOf($$0.deviation)), (App)Codec.FLOAT.fieldOf("min").forGetter($$0 -> Float.valueOf($$0.min)), (App)Codec.FLOAT.fieldOf("max").forGetter($$0 -> Float.valueOf($$0.max))).apply((Applicative)$$02, ClampedNormalFloat::new)).validate($$0 -> {
        if ($$0.max < $$0.min) {
            return DataResult.error(() -> "Max must be larger than min: [" + $$0.min + ", " + $$0.max + "]");
        }
        return DataResult.success((Object)$$0);
    });
    private final float mean;
    private final float deviation;
    private final float min;
    private final float max;

    public static ClampedNormalFloat of(float $$0, float $$1, float $$2, float $$3) {
        return new ClampedNormalFloat($$0, $$1, $$2, $$3);
    }

    private ClampedNormalFloat(float $$0, float $$1, float $$2, float $$3) {
        this.mean = $$0;
        this.deviation = $$1;
        this.min = $$2;
        this.max = $$3;
    }

    @Override
    public float sample(RandomSource $$0) {
        return ClampedNormalFloat.sample($$0, this.mean, this.deviation, this.min, this.max);
    }

    public static float sample(RandomSource $$0, float $$1, float $$2, float $$3, float $$4) {
        return Mth.clamp(Mth.normal($$0, $$1, $$2), $$3, $$4);
    }

    @Override
    public float getMinValue() {
        return this.min;
    }

    @Override
    public float getMaxValue() {
        return this.max;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.CLAMPED_NORMAL;
    }

    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min + "-" + this.max + "]";
    }
}

