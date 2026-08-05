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
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

public class TrapezoidFloat
extends FloatProvider {
    public static final MapCodec<TrapezoidFloat> CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)Codec.FLOAT.fieldOf("min").forGetter($$0 -> Float.valueOf($$0.min)), (App)Codec.FLOAT.fieldOf("max").forGetter($$0 -> Float.valueOf($$0.max)), (App)Codec.FLOAT.fieldOf("plateau").forGetter($$0 -> Float.valueOf($$0.plateau))).apply((Applicative)$$02, TrapezoidFloat::new)).validate($$0 -> {
        if ($$0.max < $$0.min) {
            return DataResult.error(() -> "Max must be larger than min: [" + $$0.min + ", " + $$0.max + "]");
        }
        if ($$0.plateau > $$0.max - $$0.min) {
            return DataResult.error(() -> "Plateau can at most be the full span: [" + $$0.min + ", " + $$0.max + "]");
        }
        return DataResult.success((Object)$$0);
    });
    private final float min;
    private final float max;
    private final float plateau;

    public static TrapezoidFloat of(float $$0, float $$1, float $$2) {
        return new TrapezoidFloat($$0, $$1, $$2);
    }

    private TrapezoidFloat(float $$0, float $$1, float $$2) {
        this.min = $$0;
        this.max = $$1;
        this.plateau = $$2;
    }

    @Override
    public float sample(RandomSource $$0) {
        float $$1 = this.max - this.min;
        float $$2 = ($$1 - this.plateau) / 2.0f;
        float $$3 = $$1 - $$2;
        return this.min + $$0.nextFloat() * $$3 + $$0.nextFloat() * $$2;
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
        return FloatProviderType.TRAPEZOID;
    }

    public String toString() {
        return "trapezoid(" + this.plateau + ") in [" + this.min + "-" + this.max + "]";
    }
}

