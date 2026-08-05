/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PowerParticleOption
implements ParticleOptions {
    private final ParticleType<PowerParticleOption> type;
    private final float power;

    public static MapCodec<PowerParticleOption> codec(ParticleType<PowerParticleOption> $$02) {
        return Codec.FLOAT.xmap($$1 -> new PowerParticleOption($$02, $$1.floatValue()), $$0 -> Float.valueOf($$0.power)).optionalFieldOf("power", (Object)PowerParticleOption.create($$02, 1.0f));
    }

    public static StreamCodec<? super ByteBuf, PowerParticleOption> streamCodec(ParticleType<PowerParticleOption> $$02) {
        return ByteBufCodecs.FLOAT.map($$1 -> new PowerParticleOption($$02, $$1.floatValue()), $$0 -> Float.valueOf($$0.power));
    }

    private PowerParticleOption(ParticleType<PowerParticleOption> $$0, float $$1) {
        this.type = $$0;
        this.power = $$1;
    }

    public ParticleType<PowerParticleOption> getType() {
        return this.type;
    }

    public float getPower() {
        return this.power;
    }

    public static PowerParticleOption create(ParticleType<PowerParticleOption> $$0, float $$1) {
        return new PowerParticleOption($$0, $$1);
    }
}

