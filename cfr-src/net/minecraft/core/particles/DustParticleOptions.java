/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.joml.Vector3f
 */
package net.minecraft.core.particles;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

public class DustParticleOptions
extends ScalableParticleOptionsBase {
    public static final int REDSTONE_PARTICLE_COLOR = 0xFF0000;
    public static final DustParticleOptions REDSTONE = new DustParticleOptions(0xFF0000, 1.0f);
    public static final MapCodec<DustParticleOptions> CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter($$0 -> $$0.color), (App)SCALE.fieldOf("scale").forGetter(ScalableParticleOptionsBase::getScale)).apply((Applicative)$$02, DustParticleOptions::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DustParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, $$0 -> $$0.color, ByteBufCodecs.FLOAT, ScalableParticleOptionsBase::getScale, DustParticleOptions::new);
    private final int color;

    public DustParticleOptions(int $$0, float $$1) {
        super($$1);
        this.color = $$0;
    }

    public ParticleType<DustParticleOptions> getType() {
        return ParticleTypes.DUST;
    }

    public Vector3f getColor() {
        return ARGB.vector3fFromRGB24(this.color);
    }
}

