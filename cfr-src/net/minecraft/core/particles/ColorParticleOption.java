/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;

public class ColorParticleOption
implements ParticleOptions {
    private final ParticleType<ColorParticleOption> type;
    private final int color;

    public static MapCodec<ColorParticleOption> codec(ParticleType<ColorParticleOption> $$02) {
        return ExtraCodecs.ARGB_COLOR_CODEC.xmap($$1 -> new ColorParticleOption($$02, (int)$$1), $$0 -> $$0.color).fieldOf("color");
    }

    public static StreamCodec<? super ByteBuf, ColorParticleOption> streamCodec(ParticleType<ColorParticleOption> $$02) {
        return ByteBufCodecs.INT.map($$1 -> new ColorParticleOption($$02, (int)$$1), $$0 -> $$0.color);
    }

    private ColorParticleOption(ParticleType<ColorParticleOption> $$0, int $$1) {
        this.type = $$0;
        this.color = $$1;
    }

    public ParticleType<ColorParticleOption> getType() {
        return this.type;
    }

    public float getRed() {
        return (float)ARGB.red(this.color) / 255.0f;
    }

    public float getGreen() {
        return (float)ARGB.green(this.color) / 255.0f;
    }

    public float getBlue() {
        return (float)ARGB.blue(this.color) / 255.0f;
    }

    public float getAlpha() {
        return (float)ARGB.alpha(this.color) / 255.0f;
    }

    public static ColorParticleOption create(ParticleType<ColorParticleOption> $$0, int $$1) {
        return new ColorParticleOption($$0, $$1);
    }

    public static ColorParticleOption create(ParticleType<ColorParticleOption> $$0, float $$1, float $$2, float $$3) {
        return ColorParticleOption.create($$0, ARGB.colorFromFloat(1.0f, $$1, $$2, $$3));
    }
}

