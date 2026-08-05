/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core.particles;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;

public class SpellParticleOption
implements ParticleOptions {
    private final ParticleType<SpellParticleOption> type;
    private final int color;
    private final float power;

    public static MapCodec<SpellParticleOption> codec(ParticleType<SpellParticleOption> $$0) {
        return RecordCodecBuilder.mapCodec($$12 -> $$12.group((App)ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("color", (Object)-1).forGetter($$0 -> $$0.color), (App)Codec.FLOAT.optionalFieldOf("power", (Object)Float.valueOf(1.0f)).forGetter($$0 -> Float.valueOf($$0.power))).apply((Applicative)$$12, ($$1, $$2) -> new SpellParticleOption($$0, (int)$$1, $$2.floatValue())));
    }

    public static StreamCodec<? super ByteBuf, SpellParticleOption> streamCodec(ParticleType<SpellParticleOption> $$02) {
        return StreamCodec.composite(ByteBufCodecs.INT, $$0 -> $$0.color, ByteBufCodecs.FLOAT, $$0 -> Float.valueOf($$0.power), ($$1, $$2) -> new SpellParticleOption($$02, (int)$$1, $$2.floatValue()));
    }

    private SpellParticleOption(ParticleType<SpellParticleOption> $$0, int $$1, float $$2) {
        this.type = $$0;
        this.color = $$1;
        this.power = $$2;
    }

    public ParticleType<SpellParticleOption> getType() {
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

    public float getPower() {
        return this.power;
    }

    public static SpellParticleOption create(ParticleType<SpellParticleOption> $$0, int $$1, float $$2) {
        return new SpellParticleOption($$0, $$1, $$2);
    }

    public static SpellParticleOption create(ParticleType<SpellParticleOption> $$0, float $$1, float $$2, float $$3, float $$4) {
        return SpellParticleOption.create($$0, ARGB.colorFromFloat(1.0f, $$1, $$2, $$3), $$4);
    }
}

